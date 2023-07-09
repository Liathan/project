// Agent hideAg in project project.mas2j

/* Initial beliefs and rules */
state(hiding).
home(5, 5). //MAGIC NUMBER
lastSeen(5, 5). //MAGIC NUMBER
/* Initial goals */

!hide.

/* Plans */
@pos[atomic]
+pos(seeker, X, Y) : state(ST) & ST \== hiding & .my_name(S) & not seen(S)[source(seeker)] <- -+lastSeen(X, Y); -pos(seeker, X, Y); -+state(hiding); .drop_all_intentions; !!hide.

@found[atomic]
+found(S)[source(seeker)] : .my_name(S) <- .print("NOOOOOOO"); .drop_all_intentions; die.
+found(S)[source(seeker)] <- .print("Bummer").

@seen[atomic]
+seen(S)[source(seeker)] : .my_name(S) <- .print("Arrivo prima"); -+state(running); .drop_all_intentions; !!run.
+seen(S)[source(seeker)] <- .print("SKill Issue").


+!run <- for(helper.NextMove(DIR)) { move(DIR);} !free.
-!run <- !run.

+!hide <- for(helper.NextMove(DIR)) { move(DIR);} !!wait.
-!hide <- !hide.

+!wait <- for(.range(X, 0, 5)) { lookAround;} !!sneak. //TODO

+!sneak <-  -+state(sneaking);
            -+num(1); 
            for(helper.NextMove(DIR))
            { 
                if(num(X) & X mod 4 == 0)
                {
                    lookAround;
                }
                if(helper.CanPeek)
                {
                    peek;
                }
                move(DIR);
                -+num(X +1);
            } 
            !free.
-!sneak <- !sneak.
            
+!free <- .my_name(S); .broadcast(tell, free(S)); .print("Sono libero"); .drop_all_intentions; die.
