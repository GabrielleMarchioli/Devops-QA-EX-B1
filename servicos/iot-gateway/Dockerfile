FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY transformacao.py gateway.py simulador.py ./
EXPOSE 9102
CMD ["python", "gateway.py"]
