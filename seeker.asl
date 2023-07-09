// Agent seeker in project project.mas2j

/* Initial beliefs and rules */
state(searching).
home(5, 5).//MAGIC NUMBER
/* Initial goals */

!count.

/* Plans */

+!count <- count; !count. // l'azione count fallisce quando ha terminato la conta
-!count <- !!seek.

+!seek <-  -+state(searching);
            -+num(1); 
            for(helper.NextMove(DIR))
            { 
                if(num(X) & X mod 4 == 0)
                {
                    lookAround;
                }
                move(DIR);
            }
            !seek. 
-!seek <- !seek.

// Quando vedo qualcuno, provo a catturarlo prima che si liberi
+!runHome <- for(helper.NextMove(DIR)) { move(DIR);}; ?seen(S); .broadcast(tell, found(S)); -+state(searching); !!seek .

@free[atomic]
+free(S)[source(S)] : seen(S) <- -seen(S); .drop_all_intentions; !!seek.

@pos[atomic]
+pos(X, _, _) : X \== seeker <- .drop_all_intentions; .broadcast(tell, seen(X)); ?myPos(A, B); .broadcast(tell, pos(seeker, A, B)); -+state(running); +seen(X); !!runHome.

+remaining(Z) : Z == 0 <- .print("THE END!!!"); .drop_all_intentions.
