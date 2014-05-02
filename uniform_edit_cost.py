import numpy as np
import matplotlib.pyplot as plt

uec = [0.01, 0.03, 0.05, 0.1, 0.2]
acc = [89.23, 89.67, 89.67, 87.69, 81.97]

plt.plot(uec, acc, 'ro-', linewidth=2)
plt.xlabel("uniform edit cost")
plt.ylabel("Accuracy")
plt.savefig("uniform_edit_cost.pdf")
