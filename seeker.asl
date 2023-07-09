// Agent seeker in project project.mas2j

/* Initial beliefs and rules */
state(searching).
home(5, 5).//MAGIC NUMBER
/* Initial goals */

!count.

/* Plans */

+!count <- count; !count. // l'azione count fallisce quando ha terminato la conta
-!count <- !!seek.

+!seek <- helper.NextMoveSeek(ACTION); ACTION; !seek.

// Quando vedo qualcuno, provo a catturarlo prima che si liberi
+!runHome : home(X, Y) & myPos(X, Y) <- ?seen(S); .broadcast(tell, found(S)); -+state(searching); !!seek.
+!runHome <- helper.NextMoveSeek(ACTION); ACTION; !runHome.
-!runHome <- !runHome.

+pos(X, _, _) : X \== seeker <- .drop_all_intentions; .broadcast(tell, seen(X)); ?myPos(A, B); .broadcast(tell, pos(seeker, A, B)); -+state(running); +seen(X); !!runHome.

+remaining(Z) : Z == 0 <- .print("THE END!!!"); .drop_all_intentions.
