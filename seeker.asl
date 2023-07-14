// Agent seeker in project project.mas2j

/* Initial beliefs and rules */
state(searching).
home(5, 5).//MAGIC NUMBER
/* Initial goals */

!count.

/* Plans */

+!count <- count; !count. // l'azione count fallisce quando ha terminato la conta
-!count <- !!seek.

+!seek <-   helper.RandomCell(X, Y);
            -+goal(X, Y);
            -+num(1); 
            for(helper.NextMove(DIR))
            { 
                if(num(N) & N mod 4 == 0)
                {
                    lookAround;
                }
                move(DIR);
            }
            !seek. 
-!seek <- !seek.

// Quando vedo qualcuno, provo a catturarlo prima che si liberi
+!runHome <- -+goal(5, 5); for(helper.NextMove(DIR)) { move(DIR);}; .findall(S, seen(S), L); for(.member(X, L)) { .broadcast(tell, found(X));} -+state(searching); !!seek .

@free[atomic]
+free(S)[source(S)] : seen(S) <- -seen(S); .drop_all_intentions; !!seek.

@pos[atomic]
+pos(X, _, _) : X \== seeker <- .drop_all_intentions; .broadcast(tell, seen(X)); ?myPos(A, B); .broadcast(tell, pos(seeker, A, B)); -+state(running); +seen(X); !!runHome.

+lost <- .print("I lost... :("); .drop_all_intentions.

+remaining(0) : not lost <- .print("I WIN!!!"); .drop_all_intentions.
