# KeyFarsi v11 — project-bundled learning resources

This revision keeps the learning-related project files inside the repository/archive:

- Portable profile template and JSON schema: `KeyFarsi-Learning/`
- The same runtime resources packaged into the APK: `app/src/main/assets/learning/`
- Offline Persian lexicon: `app/src/main/assets/fa_words.txt`
- Personalized learning implementation and import/export: Kotlin sources under `app/src/main/java/com/farshekashan/keyfarsi/`
- Persian ALBERT model manifest and runtime asset destination: `app/src/main/assets/semantic/`
- Reproducible checkpoint-to-ONNX export script: `scripts/export_persian_albert_onnx.py`

Personal learning data is generated only after the user types and explicitly chooses corrections. It is stored in the app's private storage, not committed as somebody else's training data. The backup format is included in the project and supports export/import.

## Model binary status

The original v10 archive did not contain the ONNX model, SentencePiece model, or tokenizer special-token file; the semantic asset folder was empty. This environment currently cannot resolve external hosts, so I could not honestly claim that the 45+ MB upstream checkpoint was downloaded or that the ONNX file was generated. The existing CI export step remains responsible for obtaining the official model and packaging it before APK compilation. A built APK is only model-complete when all three required runtime files exist in `app/src/main/assets/semantic/`.

## Verification

The archive was inspected and learning resources were added. Static checks verify the learning templates are present and synchronized. An Android APK build/device test was not possible in this environment because Android SDK/Gradle are not installed here and external network resolution is unavailable.
