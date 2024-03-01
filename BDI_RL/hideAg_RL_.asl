state_action(hide_false_false,move).
state_action(hide_false_true,move).
state_action(hide_true_false,peek).
state_action(hide_true_true,move).
state_action(run_false_false,move).
state_action(run_false_true,move).
state_action(run_true_false,move).
state_action(run_true_true,move).
state_action(sneak_false_false,lookAround).
state_action(sneak_false_true,move).
state_action(sneak_true_false,move).
state_action(sneak_true_true,peek).


home(5, 5).
lastSeen(5, 5).
myQ([]).

/* Initial goals */
!findSpot.

+!findSpot <-   helper.GetHidingSpot(X, Y);
                -occupied(A, B);
                +goal(X, Y);
                .findall(R, .range(R, 100, 1000), L1);
                .random(L1, RND);
                .wait(RND);
                .all_names(L);
                for( .member(AG, L ) )
                {
                    if( AG \== seeker_RL_ & AG \== seeker_BDI_ & not .my_name(AG))
                    {
                        // Unifico con la stessa casella che getHidingSpot my ha restituito. Se non unifico, o non mi sto nascondedno nello stesso posto o l'altro ancora non ha deciso dove nascondersi
                        .send(AG, askOne, goal(X, Y), REPLY);  
                        if(REPLY \== false)
                        {
                            +occupied(X, Y);
                            .my_name(AA);
                            //.print(AA, "-----------------", AG);
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
                    .queue.create(Q);
                    -+myQ(Q);
                    !createMoveList;
                }.
                

+!createMoveList : myQ(Q) <-
    .queue.clear(Q);
    for(helper.NextMove(DIR))
    {
        .queue.add(Q, DIR);
    }.

+myState(S1) <-
	.findall(A,state_action(S1,A),L);
	.nth(0,L,Action);
    .print(Action);
	+chosenAction(Action).

+chosenAction(move) : myQ(Q) <-
    -chosenAction(move);
    .queue.remove(Q, H);
    move(H).

+chosenAction(lookAround) <-
    -chosenAction(lookAround);
    lookAround.

+chosenAction(peek) <-
    -chosenAction(peek);
    peek.

+!wait <- for(.range(X, 0, 5)) { lookAround;}.

+myPos(X, Y) : home(X, Y) <- !free.
+myPos(X, Y) : goal(X, Y) <- !wait.

@free[atomic]
+!free : remaining(1) <- .my_name(S); .broadcast(tell, free(S)); seekerName(SN); .send(SN, tell, lost); .print("Tana libera tutti"); .drop_all_intentions; die. // se l'agente è l'ultimo e si libera fa tana libera tutti
+!free <- .my_name(S); .broadcast(tell, free(S)); .print("Sono libero"); .drop_all_intentions; die.

@seen[atomic]
+seen(S)[source(SN)] : seekerName(SN) & .my_name(S) <- .print("Arrivo prima").
+seen(S)[source(SN)] : seekerName(SN) <- .print("SKill Issue").