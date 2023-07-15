// Agent seeker in project project.mas2j

/* Initial beliefs and rules */
state(searching).
home(5, 5).//MAGIC NUMBER
numFound(0).
numFree(0).
lost :- numFound(FD) & numFree(FR) & FR > FD.

/* Initial goals */

!count.

/* Plans */

+!count <- count; !count. // l'azione count fallisce quando ha terminato la conta
-!count <- !!seek.

+!seek <-   helper.RandomHelp(X, Y); // con 2 argomenti unifica con una cella libera casuale
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
-!seek <- helper.RandomHelp(DIR); move(DIR); !seek. // con un solo argomento unifica con una direzione casuale

// Quando vedo qualcuno, provo a catturarlo prima che si liberi
+!runHome <-    -+goal(5, 5); 
                for(helper.NextMove(DIR)) 
                {
                    move(DIR);
                }; 
                .findall(S, seen(S), L);
                for(.member(X, L)) 
                {
                    ?numFound(F);
                    -+numFound(F +1);
                    .broadcast(tell, found(X));
                    -seen(S);
                }
                -+state(searching);
                !!seek .
-!runHome <- !runHome.

@free[atomic]
+free(S)[source(S)] : seen(S) <- -seen(S); ?numFree(F); -+numFree(F+1); .drop_all_intentions; !!seek.
+free(S)[source(S)] <- ?numFree(F); -+numFree(F+1).

@pos[atomic]
+pos(X, _, _) : X \== seeker <- .drop_all_intentions; .broadcast(tell, seen(X)); ?myPos(A, B); .broadcast(tell, pos(seeker, A, B)); -+state(running); +seen(X); !!runHome.

//+lost <- .print("I lost... :("); .drop_all_intentions.

+remaining(0) : not lost <- .print("I WIN!!!"); .drop_all_intentions.
+remaining(0) <- .print("I lost... :("); drop_all_intentions.
