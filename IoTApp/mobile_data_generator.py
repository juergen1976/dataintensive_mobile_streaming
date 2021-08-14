# Mobile sample data generator app
# Written by Juergen Toth for the sample project DataIntensive applications
import csv
import sys
import os
import time
import uuid
from datetime import datetime
from numpy.random import uniform
from random import randrange

# We accept location of storage and batch size
# Check parameters
if len(sys.argv) != 3:
    print("Please provide exactly 2 parameters. First is path where files should be written and second is batch size")
    exit()

path = sys.argv[1]
batch_size = sys.argv[2]

# Generate random GPS location
def randomGPSLocation():
   return uniform(-180,180), uniform(-90, 90)

# Generate random accelerometer values
def randomAccelerometerLocation():
  return uniform(-2,10), uniform(-2,10), uniform(-2,10)

# We write continuously every 10 seconds a new file, which simulates a new batch of data
while True:
    # Write one batch file with data
    filename = os.path.join(path, "mobile_data_" + str(time.time()) + ".csv")
    with open(filename, mode='w') as mobile_data_file:
        mobile_writer = csv.writer(mobile_data_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        mobile_writer.writerow(["timepoint", "deviceId", "acceloX","acceloY","acceloZ", "latitude", "longitude","locked"])

        for i in range(int(batch_size)):
            now = datetime.now()
            dt_string = now.strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
            gps_location = randomGPSLocation()
            accelerometerValues = randomAccelerometerLocation()
            mobile_writer.writerow([dt_string, uuid.uuid4(), accelerometerValues[0], accelerometerValues[1], accelerometerValues[2], gps_location[0], gps_location[1], randrange(2)])
    time.sleep(10)


