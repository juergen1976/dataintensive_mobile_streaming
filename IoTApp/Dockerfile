# Docker image based on python with simple python script to simulate ingested mobile data
FROM python:3
ADD mobile_data_generator.py /
RUN pip install numpy
ENTRYPOINT ["python", "-m", "mobile_data_generator.py"]