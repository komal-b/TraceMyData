FROM python:3.11-slim

WORKDIR /app

COPY . /app
# copies all files inside ml-service/ into  /app
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt



 RUN mkdir -p /app/model
# # Optional: If you want to build model at image build time




CMD ["sh", "-c", "python train_model.py && uvicorn main:app --host 0.0.0.0 --port 8000 --log-level info"]
