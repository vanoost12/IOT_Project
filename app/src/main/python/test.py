import numpy as np

def main(x,y,z):
    arr = np.array([x,y,z])
    return np.linalg.norm(arr)

if __name__ == '__main__':
    main()