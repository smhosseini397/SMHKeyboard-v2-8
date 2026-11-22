# Persian spell-checker test set (manual + JVM smoke tests)

| Typed | Expected behavior | Reason |
|---|---|---|
| سلالم | show سلام; do not force without user feedback | extra character; weighted cost 2 |
| سلاام | show سلام; do not force without user feedback | repeated character; weighted cost 2 |
| سلا م | no correction across word boundary | separate tokens |
| خووب | show خوب | repeated character |
| کیبوردد | show کیبورد | repeated character |
| خانوه | show خانه if present in lexicon | candidate ranking; verify keyboard-neighbour distance |
| میخوام | preserve colloquial form; suggest alternatives only | colloquial spelling ambiguity |
| سلامتی | preserve | valid word |
| user types a typo and taps a correction | remember exact typo -> chosen word | explicit user feedback |
| same taught typo followed by space | automatically replace with remembered word | personal correction memory |

## Safety policy

- Do not autocorrect password fields.
- Never learn a correction from a guess alone; explicit suggestion selection teaches typo mappings.
- One-cost corrections require a clear ranking margin.
- Two-cost corrections require repeated contextual support or strong personal word frequency.
- Ambiguous cases stay in the suggestion row.
