argument(a).
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
attacks(d,f).

/*argument(a).
argument(b).
argument(c).

attacks(a,b).
attacks(b,a).
attacks(c,b).*/

grounded(A) :- \+attacks(_,A), argument(A).
grounded(A) :- argument(A),
			   forall(attacks(B,A), (attacks(C,B), 
				 	 	 			 B\==C, 
				 	 	 			 A\==C, 
				 	 	 			 grounded(C))).
 
