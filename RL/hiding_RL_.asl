state_action(hide, false, false, move,	0).
state_action(hide, false, true,	 move,	0).
state_action(hide, true,  false, move,	0).
state_action(hide, true,  true,	 move,	0).
state_action(hide, false, false, lookAround,	0).
state_action(hide, false, true,	 lookAround,	0).
state_action(hide, true,  false, lookAround,	0).
state_action(hide, true,  true,	 lookAround,	0).
state_action(hide, false, false, peek, 	0).
state_action(hide, false, true,	 peek, 	0).
state_action(hide, true,  false, peek, 	0).
state_action(hide, true,  true,	 peek, 	0).

state_action(sneak, false, false, move,	0).
state_action(sneak, false, true,  move,	0).
state_action(sneak, true,  false, move,	0).
state_action(sneak, true,  true,  move,	0).
state_action(sneak, false, false, lookAround,	0).
state_action(sneak, false, true,  lookAround,	0).
state_action(sneak, true,  false, lookAround,	0).
state_action(sneak, true,  true,  lookAround,	0).
state_action(sneak, false, false, peek,	0).
state_action(sneak, false, true,  peek,	0).
state_action(sneak, true,  false, peek,	0).
state_action(sneak, true,  true,  peek,	0).

state_action(run, false, false, move,	0).
state_action(run, false, true,	move,	0).
state_action(run, true,  false, move,	0).
state_action(run, true,  true,	move,	0).
state_action(run, false, false, lookAround,	0).
state_action(run, false, true,	lookAround,	0).
state_action(run, true,  false, lookAround,	0).
state_action(run, true,  true,	lookAround,	0).
state_action(run, false, false, peek, 0).
state_action(run, false, true,	peek, 0).
state_action(run, true,  false, peek, 0).
state_action(run, true,  true,  peek, 0).



// TODO: Cambiare come gestisce gli stati e tutto il resto, dato che ho cambiato l'arità di state_action e dovrò farlo anche con myState

learning_rate(0.2).
discount_factor(0.1).
epsilon(0.3).

@startPlan[atomic]
+start <-
	.print("starting");
	+myState(standing,noSubState).

@myStatePlan[atomic]
+myState(S1,S2) : epsilon(EPSILON) <-
	.findall(action_value(V,A),state_action(S1,S2,A,V),L);
	.random(R);
	if (R < EPSILON) {
		.max(L,action_value(Value,Action));
	} else {
		.shuffle(L,L1);
		.nth(0,L1,action_value(Value,Action));
	}
	+myAction(Action);
	executeAction(Action,S1,S2).
				

@newStatePlan[atomic]
+newState(NS1,R,N) : myState(S1,S2) & myAction(A) & state_action(S1,S2,A,V) & learning_rate(ALPHA) & discount_factor(GAMMA) <-
	.findall(value(V1),state_action(NS1,S2,A1,V1),L);
	.max(L,value(Value));
	NV = V + ALPHA * (R + GAMMA * Value - V);
	-myAction(A);
	-myState(S1,S2);
	-state_action(S1,S2,A,V);
	+state_action(S1,S2,A,NV);
	+myState(NS1,S2).
											
@newSubStatePlan[atomic]
+newSubState(NS2)[source(percept)] : myState(S1,S2) & epsilon(EPSILON) <-
	-newSubState(NS2)[source(percept)];
	.findall(action_value(V,A),state_action(S1,NS2,A,V),L);
	.random(R);
	if (R < EPSILON) {
		.max(L,action_value(Value,Action));
	} else {
		.shuffle(L,L1);
		.nth(0,L1,action_value(Value,Action));
	}
	executeAction(Action,S1,NS2).
											
@rewardForSubStatePlan[atomic]
+rewardForSubState(S2,A,R,N) : learning_rate(ALPHA) & discount_factor(GAMMA) <-
	.findall(S1,state_action(S1,S2,A,V),L);
	.set.create(S);
	.set.union(S,L);
	for ( .member(X,S) ) {
		.findall(V,state_action(X,S2,A,V),L1);
		.nth(0,L1,Value);
		NV = Value + ALPHA * (R + GAMMA - Value);
		-state_action(X,S2,A,Value);
		+state_action(X,S2,A,NV)
	}.
											
@updateEpsilonPlan[atomic]
+updateEpsilon(NEW_EPSILON) : epsilon(EPSILON) <-
	-epsilon(EPSILON);
	+epsilon(NEW_EPSILON).

@showValuesPlan[atomic]
+showValues <- 
	.abolish(newState(_,_,_));
	.abolish(rewardForSubState(_,_,_,_));
	.print("******************************************************");
	.findall(S1,state_action(S1,S2,A,V),L);
	.set.create(Set);
	.set.union(Set,L);
	for ( .member(X,Set) ) {
		.findall(action_value(V,A),state_action(X,noSubState,A,V),L1);
		.max(L1,action_value(Value,Action));
		.print("state_action(",X,",",Action,")");
	}
	.findall(S2,state_action(S1,S2,A,V),L2);
	.set.create(Set1);
	.set.union(Set1,L2);
	.set.remove(Set1,noSubState);
	for ( .member(X,Set1) ) {
		.findall(action_value(V,A),state_action(S1,X,A,V),L3);
		.max(L3,action_value(Value1,Action1));
		.print("substate_action(",X,",",Action1,")");
	}.
