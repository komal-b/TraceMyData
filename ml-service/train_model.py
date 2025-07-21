import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report
import joblib
import os
from sklearn.utils.multiclass import unique_labels

DATASET_PATH = "output/privacy_dataset.csv"
MODEL_OUTPUT_PATH = "model/model.pkl"

def train_model():
    df = pd.read_csv(DATASET_PATH)

    # Encode labels
    label_encoder = LabelEncoder()
    df['privacyLabelEncoded'] = label_encoder.fit_transform(df['privacyLabel'])

    # Features to use
    feature_cols = ['numTrackers', 'trackerRisk', 'metadataRisk', 'externalOgImage', 'finalScore']
    X = df[feature_cols]
    y = df['privacyLabelEncoded']

    # Train-test split
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)

    # Train model
    model = RandomForestClassifier(random_state=42)
    model.fit(X_train, y_train)

    # Evaluate
    y_pred = model.predict(X_test)
    classes_in_test = unique_labels(y_test)

    if len(classes_in_test) > 1:
        print("📊 Classification Report:")
        print(classification_report(y_test, y_pred, target_names=label_encoder.classes_))
    else:
        print(f"⚠️ Only one class '{label_encoder.inverse_transform(classes_in_test)[0]}' present in test set. Skipping classification report.")


    # Save model and encoder
    os.makedirs(os.path.dirname(MODEL_OUTPUT_PATH), exist_ok=True)
    joblib.dump((model, label_encoder), MODEL_OUTPUT_PATH)
    print(f"✅ Model saved to {MODEL_OUTPUT_PATH}")

if __name__ == "__main__":
    train_model()
