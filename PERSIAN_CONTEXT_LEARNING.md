# KeyFarsi: on-device Persian context model

KeyFarsi now uses a compact, personalized n-gram language model that runs entirely on the device. It is a lightweight statistical language model, not a neural LLM and not ChatGPT-level semantic understanding.

## Signals used for contextual correction

1. Persian character normalization (Arabic Yeh/Kaf variants and ZWNJ-insensitive matching).
2. Weighted edit distance and Persian keyboard-neighbour substitutions to generate typo candidates.
3. Word frequency from the built-in lexicon and the user's local vocabulary.
4. Bigram counts: previous completed word -> candidate next word.
5. Trigram counts: the two previous completed words -> candidate next word. Trigrams receive a stronger ranking weight because they capture a little more local sentence context.
6. Personal typo memory: when a user explicitly selects a correction, the exact typed form is remembered.

## Learning loop

When a word is committed, KeyFarsi stores the final committed word (after any accepted automatic correction), updates its vocabulary frequency, and records adjacent bigram/trigram context. Password and other sensitive text fields are excluded. The personal n-gram store is bounded to avoid unbounded growth.

## Correction safety

The model only auto-replaces a word when the spelling candidate is close and the winner is sufficiently stronger than alternatives. A learned trigram can help resolve a two-edit typo, but ambiguous cases remain suggestions rather than being silently changed.

## Limitations

This is an on-device statistical n-gram model, not a trained neural language model. It learns the user's own word sequences over time; it does not ship with a large, professionally curated Persian sentence corpus and cannot infer arbitrary meaning as reliably as a large language model. A neural model would require a validated Persian training corpus, model conversion/quantization, and device-side latency and memory testing before it should be integrated.


## Version 7 neural personalization

A local MLP reranker has been added. Architecture: 32 input features → 12 ReLU hidden units → 1 ranking score. The feature vector combines candidate character trigrams, hashed bigram/trigram context (previous one/two words plus candidate), edit-distance/length/prefix/suffix features, and script/shape indicators.

Training is pairwise from explicit correction taps: the selected word is positive; up to five close alternatives are negative. The model performs a few small SGD updates and stores weights privately on-device. It starts with no claim of semantic ability: after three examples it may rerank suggestions, and after ten examples it may authorize an automatic replacement only when the learned score margin is clear. If the model is uncertain, the keyboard keeps the typed word and shows suggestions.

Privacy: neural weights and correction learning are local. Sensitive/password input remains excluded by the existing IME checks.

Limit: this is personalized contextual learning, not a pretrained general Persian semantic model. It cannot reliably infer unseen meanings until trained on a sufficiently large, validated Persian corpus.
