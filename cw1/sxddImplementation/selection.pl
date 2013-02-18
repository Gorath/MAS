
:- use_module(library(samsort), [samsort/3]).

%% TURN SELECTION

choose_turn([], _, opponent) :-
 !.
choose_turn(_, [], proponent) :-
 !.
choose_turn(P, O, Player) :-
 option(strategy(turn_choice), Strategy),
 turn_choice(Strategy, P, O, Player).

turn_choice(p, _, _, proponent).
turn_choice(o, _, _, opponent).

%% ARGUMENT SELECTION

proponent_arg_choice(P, Arg, PMinus) :-
 option(strategy(proponent, arg_choice), Strategy),
 argument_choice(Strategy, P, Arg, PMinus).

opponent_arg_choice(O, Arg, OMinus) :-
 option(strategy(opponent, arg_choice), Strategy),
 argument_choice(Strategy, O, Arg, OMinus).

argument_choice(o, [Arg|ArgsMinus], Arg, ArgsMinus) :-
 !.
argument_choice(n, Args, Arg, ArgsMinus) :-
 !,
 append(ArgsMinus, [Arg], Args).
argument_choice(Strategy, Args, Arg, ArgsMinus) :-
 (Strategy = s ; Strategy = l),
 !,
 sort_arguments(su, Args, SortedArgs),
 (
  Strategy = s
  -> SortedArgs = [Arg|_]
  ;  last(SortedArgs, Arg)
 ),
 delete(Args, Arg, ArgsMinus).

sort_arguments(su, Args, SortedArgs) :-
 samsort(arg_sort_ums, Args, SortedArgs).

arg_sort_ums([_,_,Su1,_]-_, [_,_,Su2,_]-_) :-
 length(Su1, L1),
 length(Su2, L2),
 L1 =< L2.

%% SENTENCE SELECTION

proponent_sentence_choice(Arg, S, ArgMinus) :-
 option(strategy(proponent, sentence_choice), Strategy),
 sentence_choice(Strategy, Arg, S, ArgMinus).

opponent_sentence_choice(Arg, S, ArgMinus) :-
 option(strategy(opponent, sentence_choice), Strategy),
 sentence_choice(Strategy, Arg, S, ArgMinus).

sentence_choice(o, [L,Sm,[S|ArgMinus],C]-LL, S, [L,Sm,ArgMinus,C]-LL).
sentence_choice(n, [L,Sm,Su,C]-LL, S, [L,Sm,SuMinus,C]-LL) :-
 append(SuMinus, [S], Su).
sentence_choice(e, [L,Sm,Su,C]-LL, S, [L,Sm,SuMinus,C]-LL) :-
 (
  (member(S, Su), assumption(S))
  -> delete(Su, S, SuMinus)
  ;  Su = [S|SuMinus]
 ).
sentence_choice(p, [L,Sm,Su,C]-LL, S, [L,Sm,SuMinus,C]-LL) :-
 (
  (member(S, Su), non_assumption(S))
  -> delete(Su, S, SuMinus)
  ;  Su = [S|SuMinus]
 ).

%% RULE SELECTION

proponent_rule_choice(Head, Body) :-
 findall(Body, rule(Head, _, Body), Bodies),
 option(strategy(proponent, rule_choice), Strategy),
 sort_rule_pairs(Strategy, Bodies, SortedBodies),
 !,
 member(Body, SortedBodies).

sort_rule_pairs(s, Bodies, SortedBodies) :-
 samsort(rule_sort_small_bodies, Bodies, SortedBodies).

rule_sort_small_bodies(Body1, Body2) :-
 length(Body1, L1),
 length(Body2, L2),
 L1 =< L2.

