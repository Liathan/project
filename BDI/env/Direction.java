package env;

import jason.environment.grid.Location;
import java.lang.Math;

public enum Direction 
{
    // L'ordine è importante. Serve nella adjDir180
    UP,
    UP_RIGHT,
    RIGHT,
    DOWN_RIGHT,
    DOWN,
    DOWN_LEFT,
    LEFT,
    UP_LEFT;

    // Direction.values() crea un array nuovo ogni volta
    // Visto che chiamo spesso la adjDir180, lo creo solo una volta
    // e lo riuso
    private static Direction[] values = Direction.values();

    public static Location nextInDir(Location l, Direction dir)
    {
        Location n = null;
        switch (dir) 
        {
            case UP:
                n =  new Location(l.x, l.y - 1);
                break;
            case DOWN:
                n =  new Location(l.x, l.y + 1);
                break;
            case RIGHT:
                n =  new Location(l.x + 1, l.y);
                break;
            case LEFT:
                n =  new Location(l.x - 1, l.y);
                break;
            case UP_LEFT:
                n =  new Location(l.x - 1, l.y -1);
                break;
            case UP_RIGHT:
                n =  new Location(l.x + 1, l.y - 1);
                break;
            case DOWN_LEFT:
                n =  new Location(l.x - 1, l.y + 1);
                break;
            case DOWN_RIGHT:
                n =  new Location(l.x + 1, l.y + 1);
                break;
        }
        return n;
    }

    // Restituisce le direzioni in 180° dalla direzione in input
    // UP -> Left, Up-left, Up, Up-right, Right; ...
    // IMPORTANTE che le restituisca nell'ordine corretto
    public static Direction[] adjDir180(Direction dir)
    {
        Direction[] ret = new Direction[5];
        int num = dir.ordinal();
        for(int i = -2; i <= 2; ++i)
        {
            // FloorMod con divisore positivo restituisce sempre un intero positivo in [0, divisore)
            ret[i+2] = values[Math.floorMod(num + i, 8)];
        }
        return ret;
    }

    // Date 2 posizioni adiacenti nella griglia, mi dice che direzione devo fare per andare da prev a next
    public static Direction whichDir(Location prev, Location next) throws Exception
    {
        // Non dovrebbe mai succedere, ma se succede almeno me ne accorgo e posso corregere
        if(! prev.isNeigbour(next))
        {
            throw new Exception(prev.toString() + " non è vicino a "+ next.toString());
        }
        if(next.x -prev.x == 1 && next.y - prev.y  == 1)
            return Direction.DOWN_RIGHT;
        if(next.x -prev.x == 0 && next.y - prev.y  == 1)
            return Direction.DOWN;
        if(next.x -prev.x == -1 && next.y - prev.y  == 1)
            return Direction.DOWN_LEFT;
        if(next.x -prev.x == -1 && next.y - prev.y  == 0)
            return Direction.LEFT;
        if(next.x -prev.x == 1 && next.y - prev.y  == 0)
            return Direction.RIGHT;
        if(next.x -prev.x == 1 && next.y - prev.y  == -1)
            return Direction.UP_RIGHT;
        if(next.x -prev.x == 0 && next.y - prev.y  == -1)
            return Direction.UP;
        if(next.x -prev.x == -1 && next.y - prev.y  == -1)
            return Direction.UP_LEFT;
        // come sopra, messa principlamente per il compilatore;
        throw new Exception("Which DIr excpetion come è possibile?");
    }
}