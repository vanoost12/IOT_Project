
import numpy as np
import torch.nn.functional as F
from sklearn.metrics import f1_score, confusion_matrix
import torch.optim as optim
import torch
from torch import nn
import pandas as pd
import os
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
from scipy.signal import find_peaks
import matplotlib.pyplot as plt

def feature_extraction(file_path,directory_path):
  df_file = pd.read_csv(os.path.join(directory_path, file_path),header=1,skip_blank_lines=True,skiprows =[0,1,2,3])
  df_file_info = pd.read_csv(os.path.join(directory_path, file_path),header=None,nrows=4,skip_blank_lines=True)
  activity = 1 if 'run' in df_file_info.iloc[2,1].lower() else 0
  features = []
  for col_name in df_file.columns:
    vals = df_file[col_name].values
    vals_lag = vals-(np.append(vals[1:],0))
    features.append(vals.mean())
    features.append(vals.std())
    features.append(vals.max())
    features.append(vals.min())
    features.append(vals_lag.mean())
    features.append(vals_lag.std())
    features.append(vals_lag.max())
    features.append(vals_lag.min())
  features.append(df_file[df_file.columns[0]].count())
  features = np.array(features)
  return features,activity

def create_dataset(directory_files):
  X = []
  y = []
  for path in directory_files:
    try:
      cur_X,cur_y = feature_extraction(path,directory_path)
      X.append(cur_X)
      y.append(cur_y)
    except Exception as error:
      continue
  X = np.stack(X)
  y = np.array(y)
  X = np.nan_to_num(X, copy=True, nan=0.0, posinf=None, neginf=None)
  return X,y

directory_path = "/content/data"
directory_files = os.listdir(directory_path)

class LSTMTagger(nn.Module):
    def __init__(self, input_size=3, hidden_size=20, num_layers=4, num_classes=2):
        super(LSTMTagger, self).__init__()
        self.hidden_size = hidden_size
        self.input_size = input_size
        self.num_layers = num_layers
        self.lstm = nn.LSTM(self.input_size, self.hidden_size, self.num_layers, batch_first=True, bidirectional=True,dropout=0.5)
        self.hidden2tag = nn.Linear(hidden_size * 2, hidden_size)
        self.hidden2tag_second = nn.Linear(hidden_size, 1)
        # self.hidden2tag_third = nn.Linear(200, 100)
        # self.hidden2tag_forth = nn.Linear(100, num_classes)
        self.activation = nn.ReLU()

    def forward_train(self, X):
        # batch
        # h0 = torch.zeros(self.num_layers * 2, X.size(0), self.hidden_size).to(device)
        # c0 = torch.zeros(self.num_layers * 2, X.size(0), self.hidden_size).to(device)
        h0 = torch.zeros(self.num_layers * 2, self.hidden_size).to(device)
        c0 = torch.zeros(self.num_layers * 2, self.hidden_size).to(device)
        # X = torch.nn.functional.normalize(X, p=2.0, dim = 0)
        lstm_out, _ = self.lstm(X, (h0, c0))
        tag_space = self.hidden2tag(lstm_out[-1])
        tag_space = self.activation(tag_space)
        tag_space = self.hidden2tag_second(tag_space)
        # tag_space = self.activation(tag_space)
        # tag_space = self.hidden2tag_third(tag_space)
        # tag_space = self.activation(tag_space)
        # tag_space = self.hidden2tag_forth(tag_space)
        # sm = nn.Softmax(dim=0)
        # tag_scores = sm(tag_space)
        return tag_space

def load_steps_csv(file_path,directory_path):
  df_file_info = pd.read_csv(os.path.join(directory_path, file_path),header=None,nrows=4)
  df_file = pd.read_csv(os.path.join(directory_path, file_path),header=1,skip_blank_lines=True,skiprows =[0,1,2,3])
  # print(df_file)
  activity = df_file_info.iloc[2,1].lower()
  actucal_steps = df_file_info.iloc[3,1]
  df_file = add_norm(df_file)
  df_file = df_file.astype('float32')
  x = df_file.to_numpy()[:,1:]
  x = np.nan_to_num(x, copy=True, nan=0.0, posinf=None, neginf=None)
  return x,activity,actucal_steps

directory_path = "/content/data"
directory_files = os.listdir(directory_path)

for path in directory_files:
    try:
      X,activity,actucal_steps = load_steps_csv(path,directory_path)
    except Exception as error:
      print(path)
      print(error)
      directory_files.remove(path)
      continue

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
input_size = 1
hidden_size = 20
num_layers = 2
num_epochs = 1
num_classes = 1
model = LSTMTagger(input_size, hidden_size, num_layers, num_classes)
model.to(device)
loss_function = nn.L1Loss()
optimizer = optim.Adam(model.parameters())


from sklearn.metrics import accuracy_score
num_epochs = 10
bad_files = []
for epoch in range(num_epochs):
    print(f"epoch number: {epoch + 1}")
    for path in directory_files:
        # t_num = path.split('_')[0]
        # if t_num in ['1','3','32']:
        #   continue
        X,activity,actucal_steps = load_steps_csv(path,directory_path)
        actucal_steps = int(actucal_steps)
        X = torch.from_numpy(X).to(device)
        labels = torch.tensor(actucal_steps).to(device)
        model.zero_grad()
        tag_scores = model.forward_train(X.float())
        print(labels, tag_scores)
        loss = loss_function(torch.squeeze(tag_scores), labels)
        loss.backward()
        optimizer.step()

    # test_paths = [i_path for i_path in directory_files if i_path.split('_')[0]=='1']
    # true_labels = []
    # pred_labels = []
    # with torch.no_grad():
    #   for path in test_paths:
    #     X,activity,actucal_steps = load_steps_csv(path,directory_path)
    #     X = torch.from_numpy(X).to(device)
    #     labels = 1 if 'run' in activity  else 0
    #     labels = torch.tensor(labels)
    #     true_labels.append(labels)
    #     model.zero_grad()
    #     tag_scores = model.forward_train(X.float())
    #     print(tag_scores)
    #     pred_labels.append(torch.argmax(tag_scores,dim=0).item())
    # acc = accuracy_score(true_labels, pred_labels)
    # print(f"Test accuracy: {acc}")


