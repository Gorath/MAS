
%% FILTERING

% only for ab- and gb-derivations

fDbyC(R, C) :-
 \+ (member(A, R),
     member(A, C)).

fDbyD(R, D, FDbyD) :-
 (
  ab_derivation
  -> subtract(D, R, FDbyD)
  ;
  gb_derivation
  -> FDbyD = R
 ).

fCbyD(A, D) :-
 \+ member(A, D).

fCbyC(L, C) :-
 \+ gb_derivation,
 member(E, L),
 member(E, C),
 !.

