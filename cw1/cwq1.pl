:- use_module(library(aggregate)).

/*argument(a).
argument(b).
argument(c).
argument(d).
argument(e).
argument(f).

attacks(b,a).	
attacks(f,a).
attacks(c,b).
attacks(d,c).
attacks(e,d).
attacks(d,f).*/


argument(a).
argument(b).
argument(c).

attacks(a,b).	
attacks(b,c).
attacks(c,b).

grounded(A) :- argument(A), groundedLoopDetector(A, []).

groundedLoopDetector(A, As) :- argument(A), \+attacks(_,A), !.
groundedLoopDetector(A, As) :- argument(A),
                          	   forall(attacks(B, A),
                              			(A \== B,
                              			 attacks(C, B),
                              			 \+member(A, As),
                              			 B \== C,
                              			 A \== C,
                              			 groundedLoopDetector(C, [A|As]))
                         		).