# SmhKeyboard IA — Sentence review before send (V1)

## Behavior
- The keyboard checks the complete text available around the cursor when its IME action is `SEND`.
- If no high-confidence Persian spelling correction is found, the send action proceeds immediately.
- If one or more high-confidence corrections are found, sending pauses and the keyboard displays the original text and the proposed corrected text.
- The user can choose **تأیید و ارسال**, **ویرایش**, or **ارسال اصلی**.
- Password fields are excluded. An active text selection is not modified.
- Text is replaced only after confirmation, in a batch edit. The editor contents are checked again before replacement to avoid overwriting changes made while the review screen was open.
- Spacing and punctuation are preserved; V1 performs conservative word-level spelling corrections across the whole text. It does not yet generate grammar rewrites or insert missing words.

## Android platform limitation
An Input Method Service can receive the host editor's `IME_ACTION_SEND` action when the user presses the keyboard action key. Android does not give an ordinary keyboard permission to intercept an unrelated Send button drawn inside the host app. Therefore, this review flow applies to the keyboard's Send action, not every app-specific on-screen button.

## Manual verification
1. Install and enable SmhKeyboard IA, then select it as the active keyboard.
2. Open a chat app whose text field exposes the keyboard action as Send (`IME_ACTION_SEND`).
3. Type a sentence containing a typo that the current dictionary confidently recognizes.
4. Press the keyboard's Send action. Confirm that sending pauses and both original and corrected text are shown.
5. Tap **ویرایش** and verify that the message remains unchanged and the normal keyboard returns.
6. Repeat and tap **ارسال اصلی**; verify that the unmodified text is sent.
7. Repeat and tap **تأیید و ارسال**; verify that the corrected text replaces the original and is sent.
8. Verify that a sentence with no confident correction sends immediately, and that password fields are never reviewed.

## Performance note
V1 uses the existing on-device dictionary, learned corrections, and conservative candidate ranking. It does not run ALBERT during the send review, to avoid unpredictable latency. Measure the actual time on target Android devices before setting a millisecond performance claim.
