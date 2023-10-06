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
	private int seekerIterations = 0;

	private double hidingEpsilon = 0.3;
	private double seekerEpsilon = 0.3;

	private int numberOfhidings = 4;
	private int numerOfCaptured = 0;
	private int moveDoneSeeker = 0; // move fatte dal seeker in stato run, dopo che ne ha fatto 11(?) do un reward molto positivo per simulare che abbia catturato
	private int actionsSinceSaw = 0; // azioni che seeker ha fatto da quando ha visto, se superano 11(?) reward negativo per simulare che l'hiding si sia lberato


	private int moveDoneHide = 0; // move fatte da hiding in stato sneak e run, dopo che ne ha fatto 11(?) do un reward molto positivo per simulare che si è liberato
	private int actionsSinceSeen = 0; // azioni che hiding ha fatto da quando è stato visto, se superano 11(?), reward negativo per simulare la cattura
	
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
				moveDoneHide++;
				newState = oneOf("hide_false_false", "hide_true_false", "sneak_false_false", "sneak_true_false", "run_false_false", "run_true_false");
			}				
			else if(state.contains("run") && actionName.equals("move")) 
			{
				moveDoneHide++;
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
			
			if (moveDoneHide == 11) // ha raggiunto casa base e si è liberato, ricomincio l'episodio
			{	
				reward = 100;
				moveDoneHide = 0;
				actionsSinceSeen = 0;
				newState = "hide_false_false";
			}
			else if(actionsSinceSeen == 11) // non ha raggiunto casa base in tempo ed è stato catturato, ricominicio l'episodio
			{
				reward = -10; // o magari -100
				moveDoneHide = 0;
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
				
			
			return true;
			
		} else if (player.startsWith("seeker") && action.getFunctor().equals("executeAction")) {
			
			if(seekerIterations >= 2000) {
				addPercept("seeker_RL_",Literal.parseLiteral("showValues"));
				return true;
			}
			
			String actionName = action.getTerm(0).toString();
			String state = action.getTerm(1).toString();
			
				
			String key = state + "_" + actionName;
			int reward;
			if (seekerPlausibleActionsForState.contains(key)) // Rimuovere (?)
				reward = 0;
			else
				reward = -1;
			
			String newState = "";
			
			if(state.contains("search") && actionName.equals("move"))
			{
				newState = oneOf("search_false", "run_false");
			}
			else if(state.contains("run") && actionName.equals("move"))
			{
				actionsSinceSaw++;
				moveDoneSeeker++;
				// Questa transizione è diversa dal file bozzaStati perchè può andare in searching solo se cattura o l'altro si libera
				// lo gestisco dopo.
				newState = "run_false";
			}
			else if(state.contains("search") && actionName.equals("lookAround"))
			{
				newState = oneOf("search_true", "run_true");
			}
			else if(state.contains("run") && actionName.equals("lookAround"))
			{
				actionsSinceSaw++;
				newState = "run_true";
			}
			else
			{
				newState = "search_false"; // azzerare le varibili
			}
			
			//TODO(?): reward shaping

			boolean lastFree = false;
			if(moveDoneSeeker == 11) // ha raggiunto casa base e cattura
			{
				reward = 10;
				moveDoneSeeker = 0;
				actionsSinceSaw = 0;
				numberOfhidings--;
				numerOfCaptured += 1;
				newState = "search_false";
			}
			else if(actionsSinceSaw == 11) // non ha raggiunto casa base in tempo e hiding si libera
			{
				reward = -10;
				moveDoneSeeker = 0;
				actionsSinceSaw = 0;
				numberOfhidings--;
				if(numberOfhidings == 0)
					lastFree = true;
				newState = "search_false";
			}

			if (numberOfhidings == 0) 
			{
				if( numerOfCaptured >= 2 && !lastFree) // seeker ha vinto
					reward = 100;
				else
					reward = -100; // ?

				numberOfhidings = 4;
				numerOfCaptured = 0;
					newState = "search_false";
			}
			
			seekerIterations++;
		
			if (seekerIterations % 100 == 0 && seekerEpsilon < 0.8) {
				removePercept("seeker_RL_",Literal.parseLiteral("updateEpsilon("+seekerEpsilon+")"));
				seekerEpsilon = seekerEpsilon + 0.05;
				addPercept("seeker_RL_",Literal.parseLiteral("updateEpsilon("+seekerEpsilon+")"));
			}
			
			addPercept("seeker_RL_",Literal.parseLiteral("newState("+newState+","+reward+","+seekerIterations+")"));

			
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
