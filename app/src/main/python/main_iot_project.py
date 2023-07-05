import numpy as np
import pandas as pd
from xgboost import XGBRegressor
from sklearn.ensemble import RandomForestClassifier
import pickle

def main(file_path):
  df_file = pd.read_csv(file_path,skip_blank_lines=True, skiprows=[0,1,2,3,4],header=1)
  norm = []
  cols = df_file.columns

  for i, row in df_file.iterrows():
      x = float(row[cols[1]])
      y = float(row[cols[2]])
      z = float(row[cols[3]])
      norm.append((x**2 + y**2 + z**2) ** 0.5)
  norm = np.array(norm)
  df_file['norm'] = norm
  features = []
  cols = df_file.columns
  for col_name in [cols[0],cols[3],cols[4]]:
    vals = df_file[col_name].values
    vals_lag = vals-(np.append(vals[1:],0))
    vals_2lag = vals-(np.append(vals[2:],[0,0]))
    vals_3lag = vals-(np.append(vals[3:],[0,0,0]))
    features.append(vals.mean())
    features.append(vals.std())
    features.append(vals.max())
    features.append(vals.min())
    features.append(vals_lag.mean())
    features.append(vals_lag.std())
    features.append(vals_lag.max())
    features.append(vals_lag.min())
    features.append(vals_2lag.mean())
    features.append(vals_2lag.std())
    features.append(vals_2lag.max())
    features.append(vals_2lag.min())
  fft = np.fft.fft(norm)
  time_vals = df_file[cols[0]].values
  dt = time_vals[1]-time_vals[0]
  n = len(norm)
  frequencies = np.fft.fftfreq(n, dt)
  features.append(len(time_vals))
  features.append(frequencies.mean())
  features.append(frequencies.std())
  features.append(frequencies.max())
  features.append(frequencies.min())
  features = np.array(features)
  X=[]
  X.append(features)
  X = np.stack(X)
  X = np.nan_to_num(X, copy=True, nan=0.0, posinf=None, neginf=None)
  activity_model = pickle.load(open('/sdcard/csv_dir/models/random_forest_model.pkl','rb'))
  activity_pred = activity_model.predict(X)

  if activity_pred == 1:
    run_model = pickle.load(open('/sdcard/csv_dir/models/steps_run_model.pkl','rb'))
    predictions = (run_model.predict(X)).round()
  else:
    walk_model = pickle.load(open('/sdcard/csv_dir/models/steps_walk_model.pkl', 'rb'))
    predictions = (walk_model.predict(X)/2).round()
  return f"{int(predictions[0])},{activity_pred[0]}"


