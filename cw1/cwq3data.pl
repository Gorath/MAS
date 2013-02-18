myAsm(free6pm_a).
myAsm(free8am_a).
myAsm(free6pm_b).
myAsm(free8am_b).
myAsm(child_a).
myAsm(sports_b).
myAsm(overweight_b).
myAsm(not_get6pm_a).
myAsm(not_get8am_a).
myAsm(not_get6pm_b).
myAsm(not_get8am_b).

contrary(free6pm_a, not_free6pm_a).
contrary(free8am_a, not_free8am_a).
contrary(child_a, not_child_a).
contrary(sports_b, not_sports_b).
contrary(overweight_b, not_overweight_b).
contrary(not_get6pm_a, get6pm_a).
contrary(not_get8am_a, get8am_a).
contrary(free6pm_b, not_free6pm_b).
contrary(free8am_b, not_free8am_b).
contrary(not_get6pm_b, get6pm_b).
contrary(not_get8am_b, get8am_b).

myRule(get6pm_a, [free6pm_a, not_get8am_a, not_get6pm_b]).
myRule(get8am_a, [free8am_a, not_get6pm_a, not_get8am_b]).
myRule(get6pm_b, [free6pm_b, not_get8am_b, not_get6pm_a]).
myRule(get8am_b, [free8am_b, not_get6pm_b, not_get8am_a]).
myRule(not_free8am_a, [child_a]).
myRule(not_free6pm_b, [sports_b]).
myRule(not_sports_b, [overweight_b]).
