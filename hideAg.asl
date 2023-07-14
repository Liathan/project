// Agent hideAg in project project.mas2j

/* Initial beliefs and rules */
state(hiding).
home(5, 5). //MAGIC NUMBER
lastSeen(5, 5). //MAGIC NUMBER

/* Initial goals */

!findSpot.

/* Plans */
@pos[atomic]
+pos(seeker, X, Y) : state(ST) & ST \== hiding & .my_name(S) & not seen(S)[source(seeker)] <- -+lastSeen(X, Y); -+state(hiding); .drop_all_intentions; helper.GetHidingSpot(A, B); -+goal(A, B); !!hide.
+pos(seeker, X, Y) : .my_name(S) & not seen(S)[source(seeker)] <- -+lastSeen(X, Y); . // se non mi ha visto e mi sto già nascondendo, cambio solo l'ultima poszione nota

@found[atomic]
+found(S)[source(seeker)] : .my_name(S) <- .print("NOOOOOOO"); .drop_all_intentions; die.
+found(S)[source(seeker)] <- .print("Bummer").

@seen[atomic]
+seen(S)[source(seeker)] : .my_name(S) <- .print("Arrivo prima"); -+state(running); .drop_all_intentions; !!run.
+seen(S)[source(seeker)] <- .print("SKill Issue").

+!findSpot <-   helper.GetHidingSpot(X, Y);
                -occupied(A, B);
                +goal(X, Y);
                .findall(R, .range(R, 100, 1000), L1);
                .random(L1, RND);
                .wait(RND);
                .all_names(L);
                for( .member(AG, L ) )
                {
                    if( AG \== seeker & not .my_name(AG))
                    {
                        // Unifico con la stessa casella che getHidingSpot my ha restituito. Se non unifico, o non mi sto nascondedno nello stesso posto o l'altro ancora non ha deciso dove nascondersi
                        .send(AG, askOne, goal(X, Y), REPLY);  
                        if(REPLY \== false)
                        {
                            +occupied(X, Y);
                            .my_name(AA);
                            .print(AA, "-----------------", AG);
                            -goal(X, Y);
                        }
                    }
                };
                if(occupied(X1, Y1))
                {
                    !!findSpot;
                }
                else
                {
                    .my_name(NAME);
                    .print(NAME, " hiding in ", X, ":", Y);
                    !!hide;
                }.
                
+!run <- -+goal(5, 5); for(helper.NextMove(DIR)) { move(DIR);} !free.
-!run <- !run.

+!hide <- for(helper.NextMove(DIR)) { move(DIR);} !!wait.
-!hide <- helper.RandomHelp(DIR); move(DIR); !hide. // se la move fallisce, vuol dire che voglio andare in una cella in cui c'è già un agente e lui non si è spostato, quindi evito il conflitto

+!wait <- for(.range(X, 0, 5)) { lookAround;} !!sneak. //TODO

+!sneak <-  -+goal(5, 5);
            -+state(sneaking);
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
-!sneak <- helper.RandomHelp(DIR); move(DIR); !sneak.
            
+!free : remaining(1) <- .my_name(S); .broadcast(tell, free(S)); .send(seeker, tell, lost); .print("Tana libera tutti"); .drop_all_intentions; die. // se l'agente è l'ultimo e si libera fa tana libera tutti
+!free <- .my_name(S); .broadcast(tell, free(S)); .print("Sono libero"); .drop_all_intentions; die.
