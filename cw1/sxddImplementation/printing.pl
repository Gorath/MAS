
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% SHOWING: DERIVATION STEPS

poss_show_case(Case) :-
 (
  verbose
  -> format('~nCase ~w~n', [Case])
  ;  true
 ).

poss_show_step(StepN, Step) :-
(
  verbose
  -> show_step(StepN, Step)
  ;  true
 ).

show_step(N, [P,O,D,C,Args,Atts]) :-
 format('*** Step ~w~n', [N]),
 format('P:    [', []),
 show_step_list_args(P),
 format('O:    [', []),
 show_step_list_args(O),
 format('D:    ~w~n', [D]),
 format('C:    ~w~n', [C]),
 format('Arg:  [', []),
 show_step_list_args(Args),
 format('Att:  [', []),
 show_step_list(Atts).

show_step_list([]) :-
 format(']~n', []).
show_step_list([X]) :-
 !,
 format('~w]~n', [X]).
show_step_list([H|T]) :-
 format('~w,~n       ', [H]),
 show_step_list(T).

show_step_list_args([]) :-
 format(']~n', []).
show_step_list_args([X]) :-
 !,
 show_arg(X),
 format(']~n', []).
show_step_list_args([H|T]) :-
 show_arg(H),
 format(',~n       ', []),
 show_step_list_args(T).

show_arg(A-Attacked) :-
 !,
 show_basic_arg(A),
 format(' --> ~w', [Attacked]).
show_arg(A) :-
 show_basic_arg(A).

show_basic_arg([L,Sm,Su,Cl]) :-
 format('~w:(~w, ~w -> ~w)', [L,Sm,Su,Cl]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% SHOWING: SOLUTIONS

show_print_result(Solution) :-
 option(print_solution, Print),
 (
  Print
  -> print_solution(Solution)
  ;  true
 ),
 option(show_solution, Show),
 (
  Show
  -> show_solution(Solution)
  ;  true
 ).

show_solution([D,C,Args,Atts]) :-
 sols(N),
 format('~n** SOLUTION ~w:~n~n', [N]),
 format('DEF:  ~w~n', [D]),
 format('CUL:  ~w~n', [C]),
 format('ARG:  [', []),
 show_step_list_args(Args),
 format('ATT:  [', []),
 show_step_list(Atts).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% PRINT TO FILE

% NOTE: This section badly needs editing

graph_colour(background,                        '#FFFFFF').

graph_colour(proponent_arg,                     '#A2DDF3').
graph_colour(proponent_asm_toBeProved,          '#7C0AA2').
graph_colour(proponent_asm,                     '#117711').
graph_colour(proponent_nonAsm_toBeProved,       '#111177').
graph_colour(proponent_nonAsm,                  '#222222').

graph_colour(opponent_finished_arg,             '#CCCCCC').
graph_colour(opponent_unfinished_arg,           '#FFFFFF').
graph_colour(opponent_ms_border,                '#000000').
graph_colour(opponent_ms_asm_culprit,           '#CC9922').
graph_colour(opponent_ms_asm_culprit_text,      '#FFFFFF').
graph_colour(opponent_ms_asm_defence,           '#117711').
graph_colour(opponent_ms_asm_defence_text,      '#FFFFFF').
graph_colour(opponent_ms_asm,                   '#77BB77').
graph_colour(opponent_ms_asm_text,              '#000000').
graph_colour(opponent_ms_nonAsm,                '#777777').
graph_colour(opponent_ms_nonAsm_text,           '#FFFFFF').
graph_colour(opponent_ums_asm_defence,          '#117711').
graph_colour(opponent_ums_asm_defence_border,   '#117711').
graph_colour(opponent_ums_asm_defence_text,     '#FFFFFF').
graph_colour(opponent_ums_asm_culprit,          '#CC9922').
graph_colour(opponent_ums_asm_culprit_border,   '#CC9922').
graph_colour(opponent_ums_asm_culprit_text,     '#FFFFFF').
graph_colour(opponent_ums_asm,                  '#77BB77').
graph_colour(opponent_ums_asm_border,           '#77BB77').
graph_colour(opponent_ums_asm_text,             '#000000').
graph_colour(opponent_ums_nonAsm,               '#AAAAAA').
graph_colour(opponent_ums_nonAsm_border,        '#AAAAAA').
graph_colour(opponent_ums_nonAsm_text,          '#000000').

graph_colour(attack_edge,                       '#BB2222').

%%

print_solution(Solution) :-
 dot_filename(FileName),
 open(FileName, write, Fd),
 print_dot_file(Solution, Fd),
 close(Fd).

dot_filename(DirAndFileName) :-
 filestem(FileStem),
 option(frameworkdir, Dir),
 option(fileID, Suff),
 atom_concat(FileStem, Suff, File),
 sols(N),
 number_codes(N, NCodes),
 atom_codes(NAtom, NCodes),
 atom_concat(File, NAtom, NumberedFile),
 atom_concat(NumberedFile, '.dot', FileName),
 atom_concat(Dir, FileName, DirAndFileName).

print_dot_file([D,C,Args,Att], Fd) :-
 dot_preliminaries(Fd),
 print_dot_args(Args, 0, D, C, Att, [], Fd),
 format(Fd, '~n}~n', []).

dot_preliminaries(Fd) :-
 format_lines([
  'digraph G {',
  ' ',
  ' ratio="fill";',
  ' compound="true";',
  ' ranksep="1";',
  ' rankdir="LR";'], Fd),
 graph_colour(background, BGCol),
 format(Fd, ' bgcolor="~w";~n', [BGCol]),
 format(Fd, ' node [style="filled",shape="box",height="0.4",width="0.6",margin="0.1,0.1"];~n', []).

print_dot_args([], _, _, _, _, _, _).
print_dot_args([[L,Sm,Su,Cl]|Args], ClusterN, D, C, Att, ArgTurns, Fd) :-
 (
  L = 1
  -> Player = proponent
  ;  memberchk(L-LL, Att),
     memberchk([LL,_,AttackedPlayer|_], ArgTurns),
     other_player(AttackedPlayer, Player)
 ),
 print_dot_arg_attacks([L,Sm,Su,Cl], ClusterN, Player, D, C, Att, ArgTurns, Fd),
 ClusterN1 is ClusterN + 1,
 print_dot_args(Args, ClusterN1, D, C, Att, [[L,ClusterN,Player,Sm]|ArgTurns], Fd).

print_dot_arg_attacks([L,Sm,Su,Cl], ClusterN, Player, D, C, Atts, ArgTurns, Fd) :-
 format(Fd, '~n subgraph cluster~w {~n', [ClusterN]),
 (
  Player = proponent
  -> PID = 'P'
  ;  PID = 'O'
 ),
 format(Fd, '  label = "~w:~w";~n', [PID,L]),
 format_lines([
  '  edge [color="#000000"];',
  '  labeljust="l";',
  '  pencolor="#444444";',
  '  style="filled";'], Fd),
 (
  Player = proponent
  -> graph_colour(proponent_arg, ArgColour)
  ;  (
      Su = []
      -> graph_colour(opponent_finished_arg, ArgColour)
      ;  graph_colour(opponent_unfinished_arg, ArgColour)
     )
 ),
 format(Fd, '  color="~w";~n', [ArgColour]),
 print_dot_arg_nodes(Sm, Su, Cl, ClusterN, Player, D, C, 0, Fd),
 format(Fd, ' }~n', []),
 graph_colour(attack_edge, AttackCol),
 format(Fd, '~n edge [color="~w"];~n', [AttackCol]),
 print_dot_attacks(L, Atts, Cl, ClusterN, ArgTurns, C, Fd).

print_dot_arg_nodes(Sm, Su, Cl, ClusterN, Player, D, C, 0, Fd) :-
 !,
 print_dot_arg_node(0, ClusterN, Cl, Player, cl, D, C, Fd),
 print_dot_arg_nodes(Sm, Su, Cl, ClusterN, Player, D, C, 1, Fd).
print_dot_arg_nodes([], [], _, _, _, _, _, _, _).
print_dot_arg_nodes([], [S|RestSu], _, ClusterN, Player, D, C, NodeN, Fd) :-
 !,
 print_dot_arg_node(NodeN, ClusterN, S, Player, ums, D, C, Fd),
 format(Fd, '  s~w_~w -> s~w_0;~n', [ClusterN,NodeN,ClusterN]),
 NodeN1 is NodeN + 1,
 print_dot_arg_nodes([], RestSu, _, ClusterN, Player, D, C, NodeN1, Fd).
print_dot_arg_nodes([S|RestSm], Su, _, ClusterN, Player, D, C, NodeN, Fd) :-
 print_dot_arg_node(NodeN, ClusterN, S, Player, ms, D, C, Fd),
 format(Fd, '  s~w_~w -> s~w_0;~n', [ClusterN,NodeN,ClusterN]),
 NodeN1 is NodeN + 1,
 print_dot_arg_nodes(RestSm, Su, _, ClusterN, Player, D, C, NodeN1, Fd).

% TO BE PROVED
print_dot_arg_node(0, 0, S, _, claim, _, _, Fd) :-
 !,
 format(Fd, '  s~0_0 ', []),
 (
  assumption(S)
  -> graph_colour(proponent_asm_toBeProved, Colour)
  ;  graph_colour(proponent_nonAsm_toBeProved, Colour)
 ),
 format(Fd, '[label="~w",fillcolor="~w",color="~w",fontcolor="white"];~n', [S,Colour,Colour]).
% PROPONENT ARGUMENTS
print_dot_arg_node(NodeN, ClusterN, S, proponent, _, _, _, Fd) :-
 format(Fd, '  s~w_~w ', [ClusterN,NodeN]),
 (
  assumption(S)
  -> graph_colour(proponent_asm, Colour)
  ;  graph_colour(proponent_nonAsm, Colour)
 ),
 format(Fd, '[label="~w",fillcolor="~w",color="~w",fontcolor="white"];~n', [S,Colour,Colour]).
% OPPONENT ARGUMENTS: CLAIM
print_dot_arg_node(0, ClusterN, Claim, opponent, cl, _, _, Fd) :-
 format(Fd, '  s~w_0 ', [ClusterN]),
 graph_colour(opponent_ms_nonAsm, FillColour),
 graph_colour(opponent_ms_border, BorderCol),
 graph_colour(opponent_ms_nonAsm_text, Font),
 format(Fd, '[label="~w",fillcolor="~w",color="~w",fontcolor="~w"];~n', [Claim,FillColour,BorderCol,Font]).
% OPPONENT ARGUMENTS: MARKED SUPPORT
print_dot_arg_node(NodeN, ClusterN, A, opponent, ms, D, C, Fd) :-
 format(Fd, '  s~w_~w ', [ClusterN,NodeN]),
 (
  % MARKED SUPPORT: CULPRIT
  member(A, C)
  -> graph_colour(opponent_ms_asm_culprit, FillColour),
     graph_colour(opponent_ms_asm_culprit_text, Font)
  ;
  % MARKED SUPPORT: DEFENCE SET
  member(A, D)
  -> graph_colour(opponent_ms_asm_defence, FillColour),
     graph_colour(opponent_ms_asm_defence_text, Font)
  ;
  % MARKED SUPPORT: ASSUMPTION (NOT DEFENCE, NOT CULPRIT)
     graph_colour(opponent_ms_asm, FillColour),
     graph_colour(opponent_ms_asm_text, Font)
 ),
 graph_colour(opponent_ms_border, BorderCol),
 format(Fd, '[label="~w",fillcolor="~w",color="~w",fontcolor="~w"];~n', [A,FillColour,BorderCol,Font]).
% OPPONENT ARGUMENTS: UNMARKED SUPPORT
print_dot_arg_node(NodeN, ClusterN, S, opponent, ums, D, C, Fd) :-
 format(Fd, '  s~w_~w ', [ClusterN,NodeN]),
 (
  assumption(S)
  -> (
      member(S, D)
      -> 
      % UNMARKED SUPPORT: DEFENCE SET
         graph_colour(opponent_ums_asm_defence, FillColour),
         graph_colour(opponent_ums_asm_defence_border, Colour),
         graph_colour(opponent_ums_asm_defence_text, Font)
      ;
      member(S, C)
      ->
      % UNMARKED SUPPORT: CULPRIT
         graph_colour(opponent_ums_asm_culprit, FillColour),
         graph_colour(opponent_ums_asm_culprit_border, Colour),
         graph_colour(opponent_ums_asm_culprit_text, Font)
      ;
      % UNMARKED SUPPORT: NON-DEFENCE SET, NON-CULPRIT
         graph_colour(opponent_ums_asm, FillColour),
         graph_colour(opponent_ums_asm_border, Colour),
         graph_colour(opponent_ums_asm_text, Font)
     )
  ;  
     % UNMARKED SUPPORT: NON-ASSUMPTION
     graph_colour(opponent_ums_nonAsm, FillColour),
     graph_colour(opponent_ums_nonAsm_border, Colour),
     graph_colour(opponent_ums_nonAsm_text, Font)
 ),
 format(Fd, '[label="~w",fillcolor="~w",color="~w",fontcolor="~w"];~n', [S,FillColour,Colour,Font]).

print_dot_attacks(L, Atts, Cl, ClusterN, ArgTurns, C, Fd) :-
 findall([AttClusterN,Sm], (member(L-LL, Atts),
                            member([LL,AttClusterN,_,Sm], ArgTurns)),
         AttClusterNs),
 print_dot_attacks_aux(AttClusterNs, Cl, ClusterN, C, Fd).

print_dot_attacks_aux([], _, _, _, _).
print_dot_attacks_aux([[AttClusterN,Sm]|RestAttClusterNs], Cl, ClusterN, C, Fd) :-
 (
  contrary(A, Cl),
  nth1(NodeN, Sm, A),
  format(Fd, ' s~w_0 -> s~w_~w;~n', [ClusterN,AttClusterN,NodeN]),
  fail
  ;
  true
 ),
 print_dot_attacks_aux(RestAttClusterNs, Cl, ClusterN, C, Fd).

%% HELPERS

other_player(proponent, opponent).
other_player(opponent, proponent).

format_lines([], _).
format_lines([Line|Rest], Fd) :-
 format(Fd, Line, []),
 format(Fd, '~n', []),
 format_lines(Rest, Fd).

number_atom(N, A) :-
 number_codes(N, Codes),
 atom_codes(A, Codes).


