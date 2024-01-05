state_action(run_false,move).
state_action(run_true,move).
state_action(search_false,lookAround).
state_action(search_true,move).

home(5, 5).
numFound(0).
numFree(0).
lost :- numFound(FD) & numFree(FR) & FR > FD.
myQ([]).

/* Initial goals */
!start.

+!start <- .my_name(SN); .broadcast(tell, seekerName(SN)); !count.

+!count <- count; !count. // l'azione count fallisce quando ha terminato la conta
-!count <-
    .queue.create(Q);
    -+myQ(Q);
    !newGoal;
    +myState(search_false).

+!newGoal : myQ(Q) <-
    .queue.clear(Q);
    helper.RandomHelp(X1, Y1);
    -+goal(X1, Y1);
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

+myPos(X, Y) : goal(X, Y) <-
    !newGoal.

@pos[atomic] 
//TODO_BDI_RL: quando gli arriva la posizione di qualcuno deve cambiare stato
+pos(X, _, _) : .my_name(S) & X \== S <- .broadcast(tell, seen(X)); ?myPos(A, B); .broadcast(tell, pos(seeker, A, B)). 