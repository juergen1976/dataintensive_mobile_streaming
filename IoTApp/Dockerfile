FROM python:3
ADD mobile_data_generator.py /
RUN pip install numpy
ENTRYPOINT ["python", "-m", "mobile_data_generator.py"]
#CMD [ "python", "./my_script.py" ]