# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
	"""
	  A reflex agent chooses an action at each choice point by examining
	  its alternatives via a state evaluation function.

	  The code below is provided as a guide.  You are welcome to change
	  it in any way you see fit, so long as you don't touch our method
	  headers.
	"""


	def getAction(self, gameState):
		"""
		You do not need to change this method, but you're welcome to.

		getAction chooses among the best options according to the evaluation function.

		Just like in the previous project, getAction takes a GameState and returns
		some Directions.X for some X in the set {North, South, West, East, Stop}
		"""
		# Collect legal moves and successor states
		legalMoves = gameState.getLegalActions()

		# Choose one of the best actions
		scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
		bestScore = max(scores)
		bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
		chosenIndex = random.choice(bestIndices) # Pick randomly among the best

		"Add more of your code here if you want to"

		return legalMoves[chosenIndex]

	def evaluationFunction(self, currentGameState, action):
		"""
		Design a better evaluation function here.

		The evaluation function takes in the current and proposed successor
		GameStates (pacman.py) and returns a number, where higher numbers are better.

		The code below extracts some useful information from the state, like the
		remaining food (newFood) and Pacman pos after moving (newPos).
		newScaredTimes holds the number of moves that each ghost will remain
		scared because of Pacman having eaten a power pellet.

		Print out these variables to see what you're getting, then combine them
		to create a masterful evaluation function.
		"""
		# Useful information you can extract from a GameState (pacman.py)
		successorGameState = currentGameState.generatePacmanSuccessor(action)
		newPos = successorGameState.getPacmanPosition()
		newFood = successorGameState.getFood()
		newGhostStates = successorGameState.getGhostStates()
		curScaredTimes = [ghostState.scaredTimer for ghostState in currentGameState.getGhostStates()]
		newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

		"*** YOUR CODE HERE ***"
		ghostDists = [util.manhattanDistance(newPos, ghostPos) for ghostPos in successorGameState.getGhostPositions()]
		ghostMinDist = min(ghostDists)

		if curScaredTimes[0] > 0:
			return successorGameState.getScore() + 1.0 / ghostMinDist

		if ghostMinDist <= 1:
			return -999999

		if len(currentGameState.getCapsules()) != 0:
			capDists = [util.manhattanDistance(newPos, capPos) for capPos in currentGameState.getCapsules()]
			capMinDist = min(capDists)

			if capMinDist <= 5:
				return 999999 - capMinDist

		if len(newFood.asList()) == 0:
			return successorGameState.getScore()

		foodDists = [util.manhattanDistance(newPos, foodPos) for foodPos in newFood.asList()]
		foodMinDist = min(foodDists)

		return successorGameState.getScore() + 1.0 / foodMinDist

def scoreEvaluationFunction(currentGameState):
	"""
	  This default evaluation function just returns the score of the state.
	  The score is the same one displayed in the Pacman GUI.

	  This evaluation function is meant for use with adversarial search agents
	  (not reflex agents).
	"""
	return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
	"""
	  This class provides some common elements to all of your
	  multi-agent searchers.  Any methods defined here will be available
	  to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

	  You *do not* need to make any changes here, but you can if you want to
	  add functionality to all your adversarial search agents.  Please do not
	  remove anything, however.

	  Note: this is an abstract class: one that should not be instantiated.  It's
	  only partially specified, and designed to be extended.  Agent (game.py)
	  is another abstract class.
	"""

	def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
		self.index = 0 # Pacman is always agent index 0
		self.evaluationFunction = util.lookup(evalFn, globals())
		self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
	"""
	  Your minimax agent (question 2)
	"""

	def minimax(self, gameState, agent, numAgents, depth):
		if depth == self.depth * numAgents:
			return (self.evaluationFunction(gameState), "")

		legalMoves = gameState.getLegalActions(agent)
		scores = []

		nextAgent = agent + 1
		if nextAgent == numAgents:
			nextAgent = 0

		for action in legalMoves:
			newState = gameState.generateSuccessor(agent, action)

			if newState.isWin() or newState.isLose():
				scores = scores + [self.evaluationFunction(newState)]
			else:
				scores = scores + [self.minimax(newState, nextAgent, numAgents, depth + 1)[0]]

		returnAction = legalMoves[0]
		returnScore = scores[0]

		if agent == 0:
			for i in range(1, len(scores)):
				if scores[i] > returnScore:
					returnAction = legalMoves[i]
					returnScore = scores[i]
		else:
			for i in range(1, len(scores)):
				if scores[i] < returnScore:
					returnAction = legalMoves[i]
					returnScore = scores[i]

		return (returnScore, returnAction)

	def getAction(self, gameState):
		"""
		  Returns the minimax action from the current gameState using self.depth
		  and self.evaluationFunction.

		  Here are some method calls that might be useful when implementing minimax.

		  gameState.getLegalActions(agentIndex):
			Returns a list of legal actions for an agent
			agentIndex=0 means Pacman, ghosts are >= 1

		  gameState.generateSuccessor(agentIndex, action):
			Returns the successor game state after an agent takes an action

		  gameState.getNumAgents():
			Returns the total number of agents in the game

		  gameState.isWin():
			Returns whether or not the game state is a winning state

		  gameState.isLose():
			Returns whether or not the game state is a losing state
		"""
		"*** YOUR CODE HERE ***"
		return self.minimax(gameState, 0, gameState.getNumAgents(), 0)[1]

class AlphaBetaAgent(MultiAgentSearchAgent):
	"""
	  Your minimax agent with alpha-beta pruning (question 3)
	"""

	def alphabeta(self, gameState, agent, numAgents, depth, alpha, beta):
		if depth == self.depth * numAgents:
			return (self.evaluationFunction(gameState), "")

		nextAgent = agent + 1
		if nextAgent == numAgents:
			nextAgent = 0

		if agent == 0:
			v = -999999
		else:
			v = 999999

		legalMoves = gameState.getLegalActions(agent)
		bestMove = ""

		for action in legalMoves:
			newState = gameState.generateSuccessor(agent, action)

			if newState.isWin() or newState.isLose():
				score = self.evaluationFunction(newState)
			else:
				score = self.alphabeta(newState, nextAgent, numAgents, depth + 1, alpha, beta)[0]

			if agent == 0:
				if score > v:
					v = score
					bestMove = action

				if v > beta:
					return (v, bestMove)

				alpha = max(alpha, v)
			else:
				if score < v:
					v = score
					bestMove = action

				if v < alpha:
					return (v, bestMove)

				beta = min(beta, v)

		return (v, bestMove)

	def getAction(self, gameState):
		"""
		  Returns the minimax action using self.depth and self.evaluationFunction
		"""
		"*** YOUR CODE HERE ***"
		return self.alphabeta(gameState, 0, gameState.getNumAgents(), 0, -999999, 999999)[1]

class ExpectimaxAgent(MultiAgentSearchAgent):
	"""
	  Your expectimax agent (question 4)
	"""

	def expectimax(self, gameState, agent, numAgents, depth):
		if depth == self.depth * numAgents:
			return (self.evaluationFunction(gameState), "")

		legalMoves = gameState.getLegalActions(agent)
		scores = []

		nextAgent = agent + 1
		if nextAgent == numAgents:
			nextAgent = 0

		for action in legalMoves:
			newState = gameState.generateSuccessor(agent, action)

			if newState.isWin() or newState.isLose():
				scores = scores + [self.evaluationFunction(newState)]
			else:
				scores = scores + [self.expectimax(newState, nextAgent, numAgents, depth + 1)[0]]

		returnAction = legalMoves[0]
		returnScore = scores[0]

		if agent == 0:
			for i in range(1, len(scores)):
				if scores[i] > returnScore:
					returnAction = legalMoves[i]
					returnScore = scores[i]
				if scores[i] == returnScore:
					if random.randint(0, 1) == 1:
						returnAction = legalMoves[i]
						returnScore = scores[i]
		else:
			returnScore = sum(scores) / (len(scores) + 0.0)

		return (returnScore, returnAction)

	def getAction(self, gameState):
		"""
		  Returns the expectimax action using self.depth and self.evaluationFunction

		  All ghosts should be modeled as choosing uniformly at random from their
		  legal moves.
		"""
		"*** YOUR CODE HERE ***"
		return self.expectimax(gameState, 0, gameState.getNumAgents(), 0)[1]

def betterEvaluationFunction(currentGameState):
	"""
	  Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
	  evaluation function (question 5).

	  DESCRIPTION: <write something here so we know what you did>
	  I took the general idea I had from the reflex agent question.
	  There is a priority of eat scared ghosts, don't get close to ghosts,
	  go for close capsules, and then go for the closest food.
	"""
	"*** YOUR CODE HERE ***"	
	pacPos = currentGameState.getPacmanPosition()
	foodGrid = currentGameState.getFood()
	ghostStates = currentGameState.getGhostStates()
	scaredTimes = [ghostState.scaredTimer for ghostState in currentGameState.getGhostStates()]

	"*** YOUR CODE HERE ***"
	ghostDists = [util.manhattanDistance(pacPos, ghostPos) for ghostPos in currentGameState.getGhostPositions()]
	ghostMinDist = min(ghostDists)

	if scaredTimes[0] > 0:
		return currentGameState.getScore() + 2.0 / (ghostMinDist + 1) + 10

	if ghostMinDist <= 1:
		return -999999

	if len(currentGameState.getCapsules()) != 0:
		capDists = [util.manhattanDistance(pacPos, capPos) for capPos in currentGameState.getCapsules()]
		capMinDist = min(capDists)

		if capMinDist <= 5:
			return currentGameState.getScore() + 0.5 / (capMinDist + 1)

	if len(foodGrid.asList()) == 0:
		return currentGameState.getScore()

	foodDists = [util.manhattanDistance(pacPos, foodPos) for foodPos in foodGrid.asList()]
	foodMinDist = min(foodDists)

	return currentGameState.getScore() + 0.5 / (foodMinDist + 1)

# Abbreviation
better = betterEvaluationFunction

