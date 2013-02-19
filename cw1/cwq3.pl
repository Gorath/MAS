:- [cwq3data].
:- use_module(library(aggregate)).

argument((Ass, [Ass])) :- myAsm(Ass). 
argument((C, X)) :- myRule(C, ReqForC),
					findall(EndAss, 
	 						(
	 						 member(A, ReqForC),
	 						 argument((A, ReqAssForA)),
	 						 member(EndAss, ReqAssForA)
	 						),
	 						X).


% X1 |- C1 attacks X2 |- C2 iff c=C(x) for some x which is x member of X2
attacks((C1,X1), (C2,X2)) :- argument((C1,X1)), argument((C2,X2)),
							 contrary(Ass,C1),
							 member(Ass,X2).

grounded(A) :- argument(A), groundedLoopDetector(A, []).

groundedLoopDetector(A, As) :- argument(A), \+attacks(_,A), !.
groundedLoopDetector(A, As) :- argument(A),
                          	   forall(attacks(B, A),
                              			(A \== B,
                              			 attacks(C, B),
                              			 \+member(A, As),
                              			 B \== C,
                              			 A \== C,
                              			 groundedLoopDetector(C, [A | As]))
                         		).
