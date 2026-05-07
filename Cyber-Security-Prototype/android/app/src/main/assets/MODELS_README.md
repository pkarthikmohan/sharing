# TFLite Model Assets

Place the following three files in this directory before building:

## Files Required

| File | Size | Source |
|---|---|---|
| `yamnet.tflite` | ~3.7 MB | Direct download |
| `sms_model.tflite` | < 5 MB | Your Colab script output |
| `vocab.txt` | < 1 MB | Your Colab script output |

---

## Download yamnet.tflite

```bash
curl -L "https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/audio_classification/android/lite-model_yamnet_classification_tflite_1.tflite" \
     -o yamnet.tflite
```

Or from Kaggle: https://www.kaggle.com/models/google/yamnet/tfLite

---

## Expected vocab.txt format

Your Colab script should output vocab.txt with ONE TOKEN PER LINE.
Line number = token index (starting from 0).

```
<PAD>        ← index 0
<OOV>        ← index 1
the          ← index 2
your         ← index 3
...
```

---

## Expected sms_model.tflite format

- **Input tensor**: int32, shape [1, SEQ_LEN]  (tokenized + padded SMS)
- **Output tensor**: float32, shape [1, 2]  → [safe_prob, scam_prob]
  OR shape [1, 1] → [scam_prob]

The classifier auto-detects the output shape at runtime.
