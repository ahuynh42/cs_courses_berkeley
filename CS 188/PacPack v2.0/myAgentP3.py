"""
Students' Names: Andrew Huynh
Phase Number: 3
In the beginning, this bot goes for the dot that is farthest from the teammate's 
predicted position if they broadcast. If they don't broadcast, i.e. when we go 
first, then we use thier current location instead. Additionally, the dots are 
filtered by the dots that the teammate plans to collect. If there are no more 
filtered dots left, then we ignore the teammate's actions and unfilter them.

When half of the dots are gone, the bot begins to go to the nearest two dots one 
after the other. At this point if the bot dies to a ghost, then the bot will 
initially go for the dot farthest from the projected destination of the teammate 
like before but will now instead continue to go to the nearest two dots.

Thge next dot to get is updated whenever tghe bot dies, the bot eats all the 
currently planned to eat dots, or if the teammate eats one of the bot's 
planned to eat dots.

While all of this is happening, if the bot moves next to a dot, then the bot gets 
that dot before going back on it's path. When there are five or less dots left, 
the bot will always go for the nearest two dots in an attempt to end the game.

The idea behind going after the dot farthest from the teammate is to try to split 
up ground. In addition, while the bot and teammate are apart from each other, the 
ghost can only go after one of them. This leaves at least one of them free to 
collect dots as they please, hopefully both if the ghost goes in circles trying
to choose between the two Pacmen.

To deal with ghosts, the bot will just 'charge' at ghosts. After watching a lot of 
games, this seems to be the most effective move as the ghost 'runs away' from the 
bot most of the time. If an action will make the bot run into a ghost, the bot will 
instead 'dodge' randomly in another direction in an attempt to get by the ghost.
"""

# myAgentP3.py
# ---------
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
# This file was based on the starter code for student bots, and refined 
# by Mesut (Xiaocheng) Yang


from captureAgents import CaptureAgent
import random, time, util
from game import Directions
import game
from util import nearestPoint

#########
# Agent #
#########
class myAgentP3(CaptureAgent):
	"""
	Students' Names: Andrew Huynh
	Phase Number: 3
	In the beginning, this bot goes for the dot that is farthest from the teammate's 
	predicted position if they broadcast. If they don't broadcast, i.e. when we go 
	first, then we use thier current location instead. Additionally, the dots are 
	filtered by the dots that the teammate plans to collect. If there are no more 
	filtered dots left, then we ignore the teammate's actions and unfilter them.

	When half of the dots are gone, the bot begins to go to the nearest two dots one 
	after the other. At this point if the bot dies to a ghost, then the bot will 
	initially go for the dot farthest from the projected destination of the teammate 
	like before but will now instead continue to go to the nearest two dots.

	Thge next dot to get is updated whenever tghe bot dies, the bot eats all the 
	currently planned to eat dots, or if the teammate eats one of the bot's 
	planned to eat dots.

	While all of this is happening, if the bot moves next to a dot, then the bot gets 
	that dot before going back on it's path. When there are five or less dots left, 
	the bot will always go for the nearest two dots in an attempt to end the game.

	The idea behind going after the dot farthest from the teammate is to try to split 
	up ground. In addition, while the bot and teammate are apart from each other, the 
	ghost can only go after one of them. This leaves at least one of them free to 
	collect dots as they please, hopefully both if the ghost goes in circles trying
	to choose between the two Pacmen.

	To deal with ghosts, the bot will just 'charge' at ghosts. After watching a lot of 
	games, this seems to be the most effective move as the ghost 'runs away' from the 
	bot most of the time. If an action will make the bot run into a ghost, the bot will 
	instead 'dodge' randomly in another direction in an attempt to get by the ghost.
	"""
	teammateFood = set()
	foodToGet = []
	futureActions = []

	far = True
	firstAction = True
	teammateDestination = None

	def registerInitialState(self, gameState):
		"""
		This method handles the initial setup of the
		agent to populate useful fields (such as what team
		we're on).

		A distanceCalculator instance caches the maze distances
		between each pair of positions, so your agents can use:
		self.distancer.getDistance(p1, p2)

		IMPORTANT: This method may run for at most 15 seconds.
		"""

		# Make sure you do not delete the following line. 
		# If you would like to use Manhattan distances instead 
		# of maze distances in order to save on initialization 
		# time, please take a look at:
		# CaptureAgent.registerInitialState in captureAgents.py.
		CaptureAgent.registerInitialState(self, gameState)
		self.start = gameState.getAgentPosition(self.index)

		self.teammateFood = set()
		self.foodToGet = []
		self.futureActions = []

		self.far = True
		self.firstAction = True
		self.teammateDestination = None

	def chooseAction(self, gameState):
		teammateActions = self.receivedBroadcast
		# Process your teammate's broadcast! 
		# Use it to pick a better action for yourself
		if teammateActions != None:
			self.firstAction = False

			teammateIndices = [index for index in self.getTeam(gameState) if index != self.index]
			assert len(teammateIndices) == 1
			teammateIndex = teammateIndices[0]
			teammatePositions = getFuturePositions(gameState, teammateActions, teammateIndex)

			if len(teammatePositions) > 0:
				self.teammateDestination = teammatePositions[len(teammatePositions) - 1]
			else:
				self.teammateDestination = gameState.getAgentPosition(teammateIndex)
			
			foodList = gameState.getFood().asList()

			for position in teammatePositions:
				if position in foodList:
					self.teammateFood.add(position)

		currentPosition = gameState.getAgentPosition(self.index)

		if currentPosition in self.foodToGet:
			self.foodToGet.remove(currentPosition)

		foodList = gameState.getFood().asList()
		newPlan = False

		if gameState.getAgentPosition(self.index) == self.start:
			self.far = True
			newPlan = True

		for food in self.foodToGet:
			if food not in foodList:
				newPlan = True
				break

		self.toBroadcast = None

		if len(self.foodToGet) == 0 or len(self.futureActions) == 0 or newPlan:
			self.foodToGet = self.nextFood(gameState)
			self.futureActions = self.getActions(gameState, self.foodToGet)
			self.toBroadcast = self.futureActions[1:]

		if len(self.futureActions) == 0:
			actions = gameState.getLegalActions(self.index)
			filteredActions = actionsWithoutStop(actions)

			if len(filteredActions) > 0:
				currentAction = random.choice(filteredActions)

				return currentAction

			return "Stop"

		# currentAction = self.futureActions[0]
		legalActions = gameState.getLegalActions(self.index)

		# if currentAction not in legalActions:
		currentAction = None
		bestDist = 9999

		for action in legalActions:
			nextState = gameState.generateSuccessor(self.index, action)
			nextPosition = nextState.getAgentPosition(self.index)

			if nextPosition in foodList:
				currentAction = action
				break

			dist = self.getMazeDistance(nextPosition, self.foodToGet[0])

			if dist < bestDist:
				currentAction = action
				bestDist = dist

		# currentAction = bestAction

		nextState = gameState.generateSuccessor(self.index, currentAction)
		nextPosition = nextState.getAgentPosition(self.index)

		ghostIndices = self.getOpponents(nextState)
		ghostPositions = [nextState.getAgentPosition(ghostIndex) for ghostIndex in ghostIndices]

		if nextPosition in ghostPositions:
			actions = gameState.getLegalActions(self.index)
			actions.remove(currentAction)
			filteredActions = actionsWithoutStop(actions)

			self.futureActions = []

			# if len(filteredActions) > 0:
			# 	currentAction = random.choice(filteredActions)

			# 	return currentAction

			# return "Stop"
			bestAction = "Stop"
			bestDist = 9999

			for action in filteredActions:
				dodgeState = gameState.generateSuccessor(self.index, action)
				dodgePosition = dodgeState.getAgentPosition(self.index)
				dist = self.getMazeDistance(dodgePosition, self.foodToGet[0])

				if dist < bestDist:
					bestAction = action
					bestDist = dist

			return bestAction

		# self.futureActions = self.futureActions[1:]

		return currentAction

	def nextFood(self, gameState):
		foodList = gameState.getFood().asList()
		currentPosition = gameState.getAgentPosition(self.index)

		for food in self.teammateFood:
			if food in foodList:
				foodList.remove(food)

		if len(foodList) == 0:
			foodList = gameState.getFood().asList()

		if len(foodList) == 1:
			return [foodList[0]]

		nextFood = None

		if self.far and len(foodList) >= 5 or len(foodList) >= 30:
			self.far = False
			bestDist = -1

			if self.firstAction:
				self.firstAction = False

				for food in foodList:
					dist = self.getMazeDistance(currentPosition, food)

					if dist > bestDist:
						nextFood = [food]
						bestDist = dist
			else:
				# temp = self.teammateDestination

				# if temp == None:
				# 	teammateIndices = [index for index in self.getTeam(gameState) if index != self.index]
				# 	assert len(teammateIndices) == 1
				# 	teammateIndex = teammateIndices[0]
				# 	temp = gameState.getAgentPosition(teammateIndex)

				for food in foodList:
					dist = self.getMazeDistance(self.teammateDestination, food)

					if dist > bestDist:
						nextFood = [food]
						bestDist = dist
		else:
			bestDist = 9999

			for food1 in foodList:
				copy1 = copyFood(foodList)
				copy1.remove(food1)
				dist1 = 2 * self.getMazeDistance(currentPosition, food1)

				for food2 in copy1:
					dist2 = dist1 + self.getMazeDistance(food1, food2)

					if dist2 < bestDist:
						nextFood = [food1, food2]
						bestDist = dist2

		# if len(foodList) <= 2:
		# 	food1 = foodList[0]

		# 	if len(foodList) == 2:
		# 		food2 = foodList[1]

		# 		if self.getMazeDistance(currentPosition, food1) <= self.getMazeDistance(currentPosition, food2):
		# 			return [food1]

		# 		return [food2]

		# 	return [food1]

		# nextFood = None
		# bestDist = 9999

		# for food1 in foodList:
		# 	copy1 = copyFood(foodList)
		# 	copy1.remove(food1)
		# 	dist1 = 2 * self.getMazeDistance(currentPosition, food1)

		# 	for food2 in copy1:
		# 		copy2 = copyFood(copy1)
		# 		copy2.remove(food2)
		# 		dist2 = dist1 + self.getMazeDistance(food1, food2)

		# 		for food3 in copy2:
		# 			dist3 = dist2 + self.getMazeDistance(food2, food3)

		# 			if dist3 < bestDist:
		# 				nextFood = [food1, food2, food3]
		# 				bestDist = dist3

		return nextFood

	def getActions(self, gameState, food):
		ret = []

		while len(food) > 0:
			actions = gameState.getLegalActions(self.index)

			bestAction = None
			bestDist = 9999

			for action in actions:
				successorGameState = gameState.generateSuccessor(self.index, action)
				newPos = successorGameState.getAgentPosition(self.index)
				dist = self.getMazeDistance(newPos, food[0])

				if dist < bestDist:
					bestAction = action

					if dist == 0:
						food = food[1:]
						break

					bestDist = dist

			if bestAction == "Stop":
				# if len(ret) == 1:
				# 	return ["Stop"]
				# 
				return ret

			gameState = gameState.generateSuccessor(self.index, bestAction)
			ret += [bestAction]

		return ret


def copyFood(foodList):
	copy = []

	for food in foodList:
		copy += [food]

	return copy

def getFuturePositions(gameState, plannedActions, agentIndex):
	"""
	Returns list of future positions given by a list of actions for a
	specific agent starting form gameState

	NOTE: this does not take into account other agent's movements
	(such as ghosts) that might impact the *actual* positions visited
	by such agent
	"""
	if plannedActions is None:
		return None

	planPositions = [gameState.getAgentPosition(agentIndex)]
	for action in plannedActions:
		if action in gameState.getLegalActions(agentIndex):
			gameState = gameState.generateSuccessor(agentIndex, action)
			planPositions.append(gameState.getAgentPosition(agentIndex))
		else:
			print("Action list contained illegal actions")
			break
	return planPositions

def actionsWithoutStop(legalActions):
	"""
	Filters actions by removing the STOP action
	"""
	legalActions = list(legalActions)
	if Directions.STOP in legalActions:
		legalActions.remove(Directions.STOP)
	return legalActions
