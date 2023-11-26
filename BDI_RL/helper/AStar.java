package helper;

import env.Direction;
import env.Model;
import jason.JasonException;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.Deque;
import java.util.logging.Logger;

public class AStar
{
    static Logger logger = Logger.getLogger(AStar.class.getName());
    Deque<Direction> path = null;
    Location initialPos;
    Location goal;

    public AStar(Location initialPos, Location goal) throws Exception
    {
        path = new LinkedList<Direction>();
        this.initialPos = initialPos;
        this.goal = goal;
        calculate();
    }

    public boolean isEmpty()
    {
        return path.isEmpty();
    }

    public Direction getNext() 
    {
        if(path.isEmpty())
            return null;
        
        return path.pop();
    }

    private void calculate() throws Exception
    {
        Model model = Model.get();
        boolean trovato = false;
        // Tengo l'associazione Location-> g(.). non serve memorizzare h, perchè è una fuznione pura
        Map<Location, Integer> gScore = new HashMap<Location, Integer>();
        Set<Location> open = new HashSet<Location>(); // La fringe
        Map<Location, Location> parent = new HashMap<Location, Location>(); // Serve per il calcolo del percorso fatto
        
        open.add(initialPos);
        gScore.put(initialPos, 0);
        while(!open.isEmpty())
        {
            Location currentNode = null;
            // Trovo il nodo con  f minore
            Integer min = Integer.MAX_VALUE;
            for(Location l : open)
            {
                //          g             +                     h
                Integer f = gScore.get(l) + l.distanceManhattan(goal);
                if(min >= f)
                {
                    min = f;
                    currentNode = l;
                }
            }

            if(currentNode.equals(goal))
            {
                trovato = true;
                break;
            }
            
            open.remove(currentNode);

            //TODO: ha senso spostarlo in Direction, e qui chiamare un metodo?
            // Genero i successori
            Location[] successors = new Location[4];
            successors[0] = Direction.nextInDir(currentNode, Direction.UP);
            successors[1] = Direction.nextInDir(currentNode, Direction.LEFT);
            successors[2] = Direction.nextInDir(currentNode, Direction.RIGHT);
            successors[3] = Direction.nextInDir(currentNode, Direction.DOWN);

            for(Location succ : successors)
            {
                // Controllo che non ci sia un muro, se c'è non è un successore, quindi salto tutto
                if(!model.isFree(model.OBSTACLE, succ))
                    continue;

                Integer tmpG = gScore.get(currentNode) + 1;
                Integer succG = gScore.get(succ);
                // Se è null, vuol dire che non ci sono mai passato e quindi va visitato
                // Se tmpG è minore, vuol dire che ho trovato un percorso migliore rispetto al precedente
                // la seconda condizione non dovrebbe essere neccessaria se l'euristica è consistente
                if( succG == null || tmpG < succG)
                {
                    parent.put(succ, currentNode);
                    gScore.put(succ, tmpG);
                    if(!open.contains(succ))
                    {
                        open.add(succ);
                    }
                }
            }
        }
        if(! trovato)
        {
            throw new Exception("Percorso non trovato");
        }
        Location tmp = goal;
        while(parent.get(tmp) != null)
        {
            // logger.info(Direction.whichDir(parent.get(tmp), tmp) +"\n"); //TEST
            path.push(Direction.whichDir(parent.get(tmp), tmp));
            tmp = parent.get(tmp);
        }
    }
}