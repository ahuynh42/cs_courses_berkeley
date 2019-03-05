import java.io.*;
import java.util.*;

public class Project {

    public static class Horse {
        int horse_num;
        int performance_rating;
        List<Integer> friends;

        public Horse(int horse_num, int performance_rating) {
            this.horse_num = horse_num;
            this.performance_rating = performance_rating;
            friends = new LinkedList<>();
        }
    }

    public static class Team {
    	String team;
    	int likelihood;
    	int num_teams;

    	public Team(String team, int likelihood, int num_teams) {
    		this.team = team;
    		this.likelihood = likelihood;
    		this.num_teams = num_teams;
    	}
    }

    public static Team greedy_algorithm(Horse[] horses) {
    	int total = horses.length;
    	int[] visited = new int[total];

    	PriorityQueue<Horse> pq = new PriorityQueue<>(total, (h1, h2) -> (h2.performance_rating - h1.performance_rating));
    	for (int i = 0; i < total; i++) {
    		pq.add(horses[i]);
    	}

    	String race_team = "";
    	int likelihood = 0;
    	int num_teams = 1;

    	while (pq.size() != 0) {
    		Horse curr = pq.poll();
    		int curr_num = curr.horse_num;

    		List<Integer> team_roster = new LinkedList<>();
    		team_roster.add(curr_num);
    		int team_rating = curr.performance_rating;
    		visited[curr_num] = 1;

    		while (true) {
    			Horse next = null;
    			int best = -1;

    			int count = curr.friends.size();
    			for (int i = 0; i < count; i++) {
    				int friend_num = curr.friends.get(i);
    				if (visited[friend_num] == 1) {
    					continue;
    				}
    				Horse friend = horses[friend_num];
    				int friend_rating = friend.performance_rating;
    				if (friend_rating > best) {
    					next = friend;
    					best = friend_rating;
    				}
    			}

    			if (best == -1) {
    				break;
    			}

    			curr = next;
    			curr_num = curr.horse_num;
    			team_roster.add(curr_num);
    			team_rating += curr.performance_rating;
    			pq.remove(curr);
    			visited[curr_num] = 1;
    		}

    		if (!race_team.equals("")) {
    			race_team += "; ";
    			num_teams++;
    		}

    		for (int i = 0; i < team_roster.size() - 1; i++) {
    			race_team += Integer.toString(team_roster.get(i)) + " ";
    		} 
    		race_team += Integer.toString(team_roster.get(team_roster.size() - 1));

    		likelihood += team_rating * team_roster.size();
    	}

    	// System.out.println("The racing team(s): " + race_team);
    	System.out.println("If this says 1, then this is automatically optimal: " + num_teams);
    	System.out.println("with likelihood of winning: " + likelihood);
        return new Team(race_team, likelihood, num_teams);
    }

    public static Team friendless_algorithm(Horse[] horses) {
    	int total = horses.length;
    	int[] visited = new int[total];

    	PriorityQueue<Horse> pq = new PriorityQueue<>(total, (h1, h2) -> (h1.friends.size() - h2.friends.size()));
    	for (int i = 0; i < total; i++) {
    		pq.add(horses[i]);
    	}

    	String race_team = "";
    	int likelihood = 0;
    	int num_teams = 1;

    	while (pq.size() != 0) {
    		Horse curr = pq.poll();
    		int curr_num = curr.horse_num;

    		List<Integer> team_roster = new LinkedList<>();
    		team_roster.add(curr_num);
    		int team_rating = curr.performance_rating;
    		visited[curr_num] = 1;

    		while (true) {
    			Horse next = null;
    			int best = 500;

    			for (int i = 0; i < total; i++) {
    				if (visited[i] == 1) {
    					continue;
    				}
    				Horse friend = horses[i];
    				if (!friend.friends.contains(curr_num)) {
    					continue;
    				}
    				int friend_friends = friend.friends.size();
    				if (friend_friends < best) {
    					next = friend;
    					best = friend_friends;
    				}
    			}

    			if (best == 500) {
    				break;
    			}

    			curr = next;
    			curr_num = curr.horse_num;
    			team_roster.add(curr_num);
    			team_rating += curr.performance_rating;
    			pq.remove(curr);
    			visited[curr_num] = 1;
    		}

    		if (!race_team.equals("")) {
    			race_team += "; ";
    			num_teams++;
    		}

    		for (int i = team_roster.size() - 1; i > 0; i--) {
    			race_team += Integer.toString(team_roster.get(i)) + " ";
    		}
    		race_team += Integer.toString(team_roster.get(0));

    		likelihood += team_rating * team_roster.size();
    	}

		// System.out.println("The racing team(s): " + race_team);
		System.out.println("If this says 1, then this is automatically optimal: " + num_teams);
    	System.out.println("with likelihood of winning: " + likelihood);
        return new Team(race_team, likelihood, num_teams);
    }

    public static void teams_algorithm(Horse[] horses) {
    	int total = horses.length;
    	int[] visited = new int[total];

    	LinkedList<Horse> ll = new LinkedList<>();
    	for (int i = 0; i < total; i++) {
    		ll.add(horses[i]);
    	}

    	while (ll.size() > 0) {
    		int size = ll.size();
    		for (int i = 0; i < size; i++) {
    			teams_helper(ll.get(i), ll.clone(), visited.copy, horses);
    		}
    	}

    	return;
    }

    public static void teams_helper(Horse curr, LinkedList<Horse> rest, int[] visited, Horse[] horses) {
    	rest.remove(curr);

    	return;
    }

    public static Team random_algorithm(Horse[] horses) {
    	int total = horses.length;
    	int[] visited = new int[total];

    	LinkedList<Horse> ll = new LinkedList<>();
    	for (int i = 0; i < total; i++) {
    		ll.add(horses[i]);
    	}

    	String race_team = "";
    	int likelihood = 0;
    	int num_teams = 1;

    	while (ll.size() != 0) {
    		int rand = (int) (Math.random() * ll.size());
    		Horse curr = ll.remove(rand);
    		int curr_num = curr.horse_num;

    		List<Integer> team_roster = new LinkedList<>();
    		team_roster.add(curr_num);
    		int team_rating = curr.performance_rating;
    		visited[curr_num] = 1;

    		while (true) {
    			LinkedList<Integer> friends = new LinkedList<>();

    			int count = curr.friends.size();
    			for (int i = 0; i < count; i++) {
    				int friend = curr.friends.get(i);
    				if (visited[friend] == 0) {
    					friends.add(friend);
    				}
    			}

    			int size = friends.size();
    			if (size == 0) {
    				break;
    			}

    			curr = horses[friends.get((int) (Math.random() * size))];
    			curr_num = curr.horse_num;
    			team_roster.add(curr_num);
    			team_rating += curr.performance_rating;
    			ll.remove(curr);
    			visited[curr_num] = 1;
    		}

    		if (!race_team.equals("")) {
    			race_team += "; ";
    			num_teams++;
    		}

    		for (int i = 0; i < team_roster.size() - 1; i++) {
    			race_team += Integer.toString(team_roster.get(i)) + " ";
    		}
    		race_team += Integer.toString(team_roster.get(team_roster.size() - 1));

    		likelihood += team_rating * team_roster.size();
    	}

    	// System.out.println("The racing team(s): " + race_team);
    	System.out.println("If this says 1, then this is automatically optimal: " + num_teams);
    	System.out.println("with likelihood of winning: " + likelihood);
        return new Team(race_team, likelihood, num_teams);
    }

    public static void main(String [] args) {
        // The name of the file to open.
        String fileName = "3.in";
        System.out.println("Running file " + fileName + ".");

        Horse[] horses = null;

        try {
        	String line = null;
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            line = bufferedReader.readLine();
            int total = Integer.parseInt(line);
            horses = new Horse[total];
            System.out.println("There are " + total + " horses in this file.");
            System.out.println();

            int curr_horse = 0;

            while ((line = bufferedReader.readLine()) != null) {
                String[] words = line.split(" ");

                horses[curr_horse] = new Horse(curr_horse, Integer.parseInt(words[curr_horse]));

                for (int i = 0; i < total; i++) {
                    if (i == curr_horse) {
                        continue;
                    }
                    if (Integer.parseInt(words[i]) == 1) {
                        horses[curr_horse].friends.add(i);
                    }
                }

                curr_horse++;
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + fileName + "'.");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'.");                  
            ex.printStackTrace();
        }

        PriorityQueue<Team> pq = new PriorityQueue<>(1, (t1, t2) -> (t2.likelihood - t1.likelihood));

        System.out.println("Running greedy algorithm.");
        pq.add(greedy_algorithm(horses));
        System.out.println();

        System.out.println("Running friendless algorithm.");
        pq.add(friendless_algorithm(horses));
        System.out.println();

        for (int i = 1; i < horses.length * 10 + 1; i++) {
        	System.out.println("Running random algorithm " + i + ".");
        	pq.add(random_algorithm(horses));
        	System.out.println();
        }

        Team ret = pq.poll();

        System.out.println("Final choice team is:");
        // System.out.println(ret.team);
        System.out.println("If this says 1, then this is automatically optimal: " + ret.num_teams);
        System.out.println("with likelihood of winning " +  ret.likelihood);
    }

}