import jason.environment.Environment;
import jason.asSyntax.*;
import java.util.*;
import java.util.logging.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TrainingEnv extends Environment {

	private Logger logger = Logger.getLogger("rl_training.mas2j."+TrainingEnv.class.getName());
	private int hidingIterations = 0;
	// private int hidingSubIterations = 0;
	private int seekerIterations = 0;
	// private int seekerSubIterations = 0;
	private double hidingEpsilon = 0.3;
	private double seekerEpsilon = 0.3;

	// private int numberOfTasks = 5;
	// private int numberOfseekers = 4;
	private int numberOfhidings = 4;
	
	private List<String> hidingPlausibleActionsForState = new ArrayList<String>();
	private List<String> seekerPlausibleActionsForState = new ArrayList<String>();
	
	@Override
	public void init(String[] args) {
		
		hidingPlausibleActionsForState.add("standing_look");
		hidingPlausibleActionsForState.add("standing_move");
		hidingPlausibleActionsForState.add("taskDetected_look");
		hidingPlausibleActionsForState.add("taskDetected_repair");
		hidingPlausibleActionsForState.add("taskDetected_move");
		hidingPlausibleActionsForState.add("nothingToDo_look");
		hidingPlausibleActionsForState.add("nothingToDo_move");
		hidingPlausibleActionsForState.add("wasFixing_trust");
		hidingPlausibleActionsForState.add("wasKilling_untrust");
		hidingPlausibleActionsForState.add("reported_trustAccuser_voteForAccuser");
		hidingPlausibleActionsForState.add("reported_trustAccuser_voteForAccused");
		hidingPlausibleActionsForState.add("reported_trustAccuser_dontVote");
		hidingPlausibleActionsForState.add("reported_trustAccused_voteForAccuser");
		hidingPlausibleActionsForState.add("reported_trustAccused_voteForAccused");
		hidingPlausibleActionsForState.add("reported_trustAccused_dontVote");
		hidingPlausibleActionsForState.add("reported_untrustAccuser_voteForAccuser");
		hidingPlausibleActionsForState.add("reported_untrustAccuser_voteForAccused");
		hidingPlausibleActionsForState.add("reported_untrustAccuser_dontVote");
		hidingPlausibleActionsForState.add("reported_untrustAccused_voteForAccuser");
		hidingPlausibleActionsForState.add("reported_untrustAccused_voteForAccused");
		hidingPlausibleActionsForState.add("reported_untrustAccused_dontVote");
		hidingPlausibleActionsForState.add("reported_dontknow_dontVote");
		hidingPlausibleActionsForState.add("advantageReceived_report");
		
		seekerPlausibleActionsForState.add("standing_look");
		seekerPlausibleActionsForState.add("standing_move");
		seekerPlausibleActionsForState.add("found1_look");
		seekerPlausibleActionsForState.add("found1_deceive");
		seekerPlausibleActionsForState.add("found1_kill");
		seekerPlausibleActionsForState.add("found1_move");
		seekerPlausibleActionsForState.add("found2orMore_look");
		seekerPlausibleActionsForState.add("found2orMore_deceive");
		seekerPlausibleActionsForState.add("found2orMore_kill");
		seekerPlausibleActionsForState.add("found2orMore_move");
		seekerPlausibleActionsForState.add("goalAccomplished_look");
		seekerPlausibleActionsForState.add("goalAccomplished_move");
		seekerPlausibleActionsForState.add("notFound_look");
		seekerPlausibleActionsForState.add("notFound_move");
		seekerPlausibleActionsForState.add("advantageReceived_report");
		seekerPlausibleActionsForState.add("reported_dontVote");
		
		addPercept("hiding_RL_",Literal.parseLiteral("start"));
		addPercept("seeker_RL_",Literal.parseLiteral("start"));
		
		// new Timer().scheduleAtFixedRate(new TimerTask() {
		// 	private int runs = 0;
		// 	public void run() {
		// 		String newSubState = oneOf("wasFixing","wasKilling","reported_trustAccuser","reported_trustAccused","reported_untrustAccuser","reported_untrustAccused","reported_dontknow","advantageReceived");
		// 		removePercept("hiding_RL_",Literal.parseLiteral("newSubState("+newSubState+")"));
		// 		addPercept("hiding_RL_",Literal.parseLiteral("newSubState("+newSubState+")"));
		// 		if (++runs > 2000)
		// 			cancel();
		// 	}
		// }, 0, 500);
		
		// new Timer().scheduleAtFixedRate(new TimerTask() {
		// 	private int runs = 0;
		// 	public void run() {
		// 		String newSubState = oneOf("advantageReceived","reported");
		// 		removePercept("seeker_RL_",Literal.parseLiteral("newSubState("+newSubState+")"));
		// 		addPercept("seeker_RL_",Literal.parseLiteral("newSubState("+newSubState+")"));
		// 		if (++runs > 500)
		// 			cancel();
		// 	}
		// }, 0, 500);
		
	}
	
	@Override
	 public void stop() {
		 super.stop();
	 }
	 
	 @Override
	public synchronized boolean executeAction(String player, Structure action) {
		
		if (player.startsWith("hiding") && action.getFunctor().equals("executeAction")) {
			
			if(hidingIterations >= 500) { //TODO: cambiare numero (?)
				addPercept("hiding_RL_",Literal.parseLiteral("showValues"));
				return true;
			}
			
			String actionName = action.getTerm(0).toString();
			String state = action.getTerm(1).toString();
			// String subState = action.getTerm(2).toString();
			
			// if(subState.equals("noSubState")) {
				
				String key = state + "_" + actionName;
				int reward;
				if (hidingPlausibleActionsForState.contains(key))
					reward = 0;
				else
					reward = -1;
				
				String newState = "";
				
				if (key.equals("standing_look"))
					newState = oneOf("taskDetected","nothingToDo");
				else if (state.equals("taskDetected") && !key.equals("taskDetected_repair"))
					newState = "taskDetected";
				else if (state.equals("nothingToDo") && !key.equals("nothingToDo_move"))
					newState = "nothingToDo";
				else
					newState = "standing";
				
				if (key.equals("taskDetected_repair")) {
					reward = 1;
					numberOfTasks--;
				}
				
				if (numberOfTasks == 0) {	//episode concluded, starting new episode
					reward = 100;
					numberOfTasks = 5;
				}
				
				hidingIterations++;
				
				if (hidingIterations % 100 == 0 && hidingEpsilon < 0.8) {
					removePercept("hiding_RL_",Literal.parseLiteral("updateEpsilon("+hidingEpsilon+")"));
					hidingEpsilon = hidingEpsilon + 0.05;
					addPercept("hiding_RL_",Literal.parseLiteral("updateEpsilon("+hidingEpsilon+")"));
				}
			
				addPercept("hiding_RL_",Literal.parseLiteral("newState("+newState+","+reward+","+hidingIterations+")"));
				
			// } else {
				
			// 	String key = subState + "_" + actionName;
			// 	int reward;
			// 	if (hidingPlausibleActionsForState.contains(key))
			// 		reward = 0;
			// 	else
			// 		reward = -1;
				
			// 	if (key.equals("reported_trustAccused_voteForAccuser") || key.equals("reported_trustAccuser_voteForAccused")
			// 			|| key.equals("reported_untrustAccused_voteForAccused") || key.equals("reported_untrustAccuser_voteForAccuser")) {
			// 		Random rand = new Random();
			// 		int result = rand.nextInt(2);
			// 		if (result == 1) {
			// 			numberOfseekers--;
			// 			reward = 1;
			// 		}
			// 	}
				
			// 	if (numberOfseekers == 0) {	//episode concluded, starting new episode
			// 		reward = 100;
			// 		numberOfseekers = 4;
			// 	}
				
			// 	hidingSubIterations++;
				
			// 	addPercept("hiding_RL_",Literal.parseLiteral("rewardForSubState("+subState+","+actionName+","+reward+","+hidingSubIterations+")"));
			// }
			
			return true;
			
		} else if (player.startsWith("seeker") && action.getFunctor().equals("executeAction")) {
			
			if(seekerIterations >= 2000) {
				addPercept("seeker_RL_",Literal.parseLiteral("showValues"));
				return true;
			}
			
			String actionName = action.getTerm(0).toString();
			String state = action.getTerm(1).toString();
			// String subState = action.getTerm(2).toString();
			
			// if (subState.equals("noSubState")) {
				
				String key = state + "_" + actionName;
				int reward;
				if (seekerPlausibleActionsForState.contains(key))
					reward = 0;
				else
					reward = -1;
				
				String newState = "";
				
				if (key.equals("standing_look"))
					newState = oneOf("found1","found2orMore","notFound");
				else if (key.equals("found1_look") || key.equals("found1_report") || key.equals("found1_dontVote"))
					newState = "found1";
				else if (key.equals("found1_deceive") || key.equals("found1_kill"))
					newState = "goalAccomplished";
				else if (key.equals("found2orMore_look") || key.equals("found2orMore_report") || key.equals("found2orMore_dontVote"))
					newState = "found2orMore";
				else if (key.equals("found2orMore_deceive") || key.equals("found2orMore_kill"))
					newState = "goalAccomplished";
				else if (state.equals("goalAccomplished") && !key.equals("goalAccomplished_move"))
					newState = "goalAccomplished";
				else if (action.equals("notFound") && !key.equals("notFound_move"))
					newState = "notFound";
				else
					newState = "standing";
				
				if (key.equals("found1_kill")) {
					reward = 10;
					numberOfhidings--;
				}
				
				if (key.equals("found2orMore_deceive")) {
					reward = 1;
				}
			
				if (numberOfhidings == 0) {
					reward = 100;
					numberOfhidings = 4;
				}
				
				seekerIterations++;
			
				if (seekerIterations % 100 == 0 && seekerEpsilon < 0.8) {
					removePercept("seeker_RL_",Literal.parseLiteral("updateEpsilon("+seekerEpsilon+")"));
					seekerEpsilon = seekerEpsilon + 0.05;
					addPercept("seeker_RL_",Literal.parseLiteral("updateEpsilon("+seekerEpsilon+")"));
				}
				
				addPercept("seeker_RL_",Literal.parseLiteral("newState("+newState+","+reward+","+seekerIterations+")"));

			// } else {
				
			// 	String key = subState + "_" + actionName;
			// 	int reward;
			// 	if (seekerPlausibleActionsForState.contains(key))
			// 		reward = 0;
			// 	else
			// 		reward = -1;
				
			// 	seekerSubIterations++;
				
			// 	addPercept("seeker_RL_",Literal.parseLiteral("rewardForSubState("+subState+","+actionName+","+reward+","+seekerSubIterations+")"));
			// }
			
			return true;
			
		} else {
			//this should never happen
			logger.info("action not implemented");
			return false;
		}
	}
	
	private String oneOf(String... args) {
		
		Random rand = new Random();
		int result = rand.nextInt(args.length);
		return args[result];
	}
}
