argument((Ass, [Ass])) :- myAsm(Ass). 
argument((C, X)) :- myRule(C, ReqForC),
					findall(EndAss, 
	 						(
	 						 member(A, ReqForC),
	 						 argument((A, ReqAssForA)),
	 						 member(EndAss, ReqAssForA)
	 						),
	 						X).