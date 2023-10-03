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

	private int moveDone = 0; // mosse fatte da hiding in stato sneak e run, dopo che ne ha fatto 11(?) do un reward molto positivo per simulare che si è liberato
	private int actionsSinceSeen = 0; // azioni che hiding ha fatto da quando è stato visto, se superano 11(?), reward negativo per simulare la cattura
	private int actionsSinceSaw = 0; // azioni che seeker ha fatto da quando ha visto, se superano 11(?) reward negativo per simulare che l'hiding si sia lberato
	
	private List<String> hidingPlausibleActionsForState = new ArrayList<String>();
	private List<String> seekerPlausibleActionsForState = new ArrayList<String>(); // Probabilmente inutile: in ogni stato tutte le azioni sono valide
	
	@Override
	public void init(String[] args) {
		
		// Il primo booleano dice se può fare la peek nello stato, quindi se è false la peek non è valida
		// Forse ha senso non usare una lista di stati plausibili ma controllare il primo bool 
		hidingPlausibleActionsForState.add("hide_false_false_move");
		hidingPlausibleActionsForState.add("hide_false_false_lookAround");
		hidingPlausibleActionsForState.add("hide_false_true_move");
		hidingPlausibleActionsForState.add("hide_false_true_lookAround");
		hidingPlausibleActionsForState.add("hide_true_false_move");
		hidingPlausibleActionsForState.add("hide_true_false_peek");
		hidingPlausibleActionsForState.add("hide_true_false_lookAround");
		hidingPlausibleActionsForState.add("hide_true_true_move");
		hidingPlausibleActionsForState.add("hide_true_true_peek");
		hidingPlausibleActionsForState.add("hide_true_true_lookAround");

		hidingPlausibleActionsForState.add("sneak_false_false_move");
		hidingPlausibleActionsForState.add("sneak_false_false_lookAround");
		hidingPlausibleActionsForState.add("sneak_false_true_move");
		hidingPlausibleActionsForState.add("sneak_false_true_lookAround");
		hidingPlausibleActionsForState.add("sneak_true_false_move");
		hidingPlausibleActionsForState.add("sneak_true_false_peek");
		hidingPlausibleActionsForState.add("sneak_true_false_lookAround");
		hidingPlausibleActionsForState.add("sneak_true_true_move");
		hidingPlausibleActionsForState.add("sneak_true_true_peek");
		hidingPlausibleActionsForState.add("sneak_true_true_lookAround");

		hidingPlausibleActionsForState.add("run_false_false_move");
		hidingPlausibleActionsForState.add("run_false_false_lookAround");
		hidingPlausibleActionsForState.add("run_false_true_move");
		hidingPlausibleActionsForState.add("run_false_true_lookAround");
		hidingPlausibleActionsForState.add("run_true_false_move");
		hidingPlausibleActionsForState.add("run_true_false_peek");
		hidingPlausibleActionsForState.add("run_true_false_lookAround");
		hidingPlausibleActionsForState.add("run_true_true_move");
		hidingPlausibleActionsForState.add("run_true_true_peek");
		hidingPlausibleActionsForState.add("run_true_true_lookAround");
		
		seekerPlausibleActionsForState.add("search_false_move"); // Probabilmente tutte inutili: vedi sopra
		seekerPlausibleActionsForState.add("search_false_lookAround");
		seekerPlausibleActionsForState.add("search_true_move");
		seekerPlausibleActionsForState.add("search_true_lookAround");
		seekerPlausibleActionsForState.add("run_false_move");
		seekerPlausibleActionsForState.add("run_false_lookAround");
		seekerPlausibleActionsForState.add("run_true_move");
		seekerPlausibleActionsForState.add("run_true_lookAround");
		
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
				
				//TODO: cambiare le probabilità a quelli che servono
				if(state.contains("hide") && actionName.equals("move"))
				{
					newState = oneOf("hide_false_false", "hide_true_false", "sneak_false_false", "sneak_true_false", "run_false_false", "run_true_false");
				}
				// anceh se sono uguali, sono due if diversi in caso vogliamo cambiare probabilità indipendentemente
				else if(state.contains("sneak") && actionName.equals("move")) 
				{
					moveDone++;
					newState = oneOf("hide_false_false", "hide_true_false", "sneak_false_false", "sneak_true_false", "run_false_false", "run_true_false");
				}				
				else if(state.contains("run") && actionName.equals("move")) 
				{
					moveDone++;
					actionsSinceSeen++;
					newState = oneOf("run_false_false", "run_true_false");
				}
				// confronta il file bozzaStati sul perchè possa andare anche in stati X_true_true
				else if(state.contains("hide_false") && actionName.equals("lookAround"))
				{
					newState = oneOf("hide_false_true", "hide_true_true", "run_false_true", "run_true_true");
				}
				else if(state.contains("hide_true") && actionName.equals("lookAround"))
				{
					newState = oneOf("hide_true_true", "run_true_true");
				}
				// confronta il file bozzaStati sul perchè possa andare anche in stati X_true_true
				else if(state.contains("sneak_false") && actionName.equals("lookAround"))
				{
					newState = oneOf("hide_false_true", "hide_true_true", "sneak_false_true", "sneak_true_true", "run_false_true", "run_true_true");
				}
				else if(state.contains("sneak_true") && actionName.equals("lookAround"))
				{
					newState = oneOf("hide_true_true", "sneak_true_true", "run_true_true");
				}
				// confronta il file bozzaStati sul perchè possa andare anche in run_true_true
				else if(state.contains("run_false") && actionName.equals("lookAround"))
				{
					actionsSinceSeen++;
					newState = oneOf("run_false_true", "run_true_true");
				}
				else if(state.contains("run_true") && actionName.equals("lookAround"))
				{
					actionsSinceSeen++;
					newState = "run_true_true";
				}
				else if(state.contains("hide") && actionName.equals("peek"))
				{
					newState = oneOf("hide_false_false", "run_false_false");
				}
				else if(state.contains("sneak") && actionName.equals("peek"))
				{
					newState = oneOf("hide_false_false", "sneak_false_false", "run_false_false");
				}
				else if(state.contains("run") && actionName.equals("peek"))
				{
					actionsSinceSeen++;
					newState = "run_false_false";
				}
				else
					newState = "hide_false_false"; //TODO(?): azzerare le variabili che tengono conto delle mosse
				

				// TODO: reward shaping ?
				
				if (moveDone == 11) // ha raggiunto casa base e si è liberato, ricomincio l'episodio
				{	
					reward = 100;
					moveDone = 0;
					actionsSinceSeen = 0;
					newState = "hide_false_false";
				}
				else if(actionsSinceSeen == 11) // non ha raggiunto casa base in tempo ed è stato catturato, ricominicio l'episodio
				{
					reward = -10; // o magari -100
					moveDone = 0;
					actionsSinceSeen = 0;
					newState = "hide_false_false";
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
				if (seekerPlausibleActionsForState.contains(key)) // Rimuovere (?)
					reward = 0;
				else
					reward = -1;
				
				String newState = "";
				
				//TODO: cambiare transizioni
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
