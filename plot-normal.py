import numpy as np
import matplotlib.pyplot as plt

e_x = [0.1, 0.5, 0.75, 1, 1.3, 1.5, 2]
e_y = [70.54, 83.76, 85.27, 87.03, 87.9, 87.03, 85.49]

u_x = [0.1, 0.5, 0.75, 1, 1.5]
u_y = [73, 87.9, 87.69, 85.27, 81.09]

plt.plot(e_x, e_y, 'r^-', u_x, u_y, 'bo-', linewidth=2)
plt.legend(("empirical", "uniform"), loc=4)
plt.xlabel("mu")
plt.ylabel("Accuracy")
plt.savefig("normal.pdf")
