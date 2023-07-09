// Agent hideAg in project project.mas2j

/* Initial beliefs and rules */
state(hiding).
home(5, 5). //MAGIC NUMBER
lastSeen(5, 5). //MAGIC NUMBER
/* Initial goals */

!act.

/* Plans */

+pos(seeker, X, Y) <- -+lastSeen(X, Y); -pos(seeker, X, Y).
+found(S)[source(seeker)] : .my_name(S) <- .print("NOOOOOOO"); .drop_all_intentions; die. // TODO: rimouverlo
+found(S)[source(seeker)] <- .print("Bummer").

+seen(S)[source(seeker)] : .my_name(S) <- .print("Arrivo prima"); -+state(running); .drop_all_intentions; !!act.
+seen(S)[source(seeker)] <- .print("SKill Issue").

+libero(S)[source(O)]. // TODO: rimuoverlo dalla lista degli agenti rimantenti

//TODO: controllare per la tana libera tutti
+!act : myPos(X, Y) & home(X, Y) <- .my_name(S); .broadcast(tell, free(S)); die. // Se l'agente è arrivato alla tana si libera e viene rimosso dal gioco
+!act <- helper.NextMoveHide(ACTION); ACTION; !act. // l'azione interna si occupa di dove andare e come
-!act <- !act.

