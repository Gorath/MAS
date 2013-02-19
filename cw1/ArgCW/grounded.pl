
:- use_module(library(aggregate)).

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