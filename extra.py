import numpy as np
import matplotlib.pyplot as plt

e_x = [0.1, 0.5, 0.75, 0.8, 0.9, 1.0, 1.1, 1.3, 1.5, 2.0]
e_y = [70.10, 85.00, 86.8, 87.69, 88.13, 89.67, 89.45, 89.23, 87.9, 84.39]

u_x = [0.1, 0.3, 0.5, 0.8, 1, 1.5, 2]
u_y = [74.5, 86.15, 89.67, 89.67, 86.59, 78.9, 70.32]

plt.plot(e_x, e_y, 'r^-', u_x, u_y, 'bo-', linewidth=2)
plt.legend(("empirical", "uniform"), loc=4)
plt.xlabel("mu")
plt.ylabel("Accuracy")
plt.savefig("extra.pdf")
