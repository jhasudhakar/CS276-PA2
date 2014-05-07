pdf: fig report.tex
	pdflatex report.tex
	pdflatex report.tex && open report.pdf
fig: plot-extra.py plot-normal.py plot-uniform-cost.py
	python plot-extra.py
	python plot-normal.py
	python plot-uniform-cost.py
clean:
	rm report.aux report.log report.pdf extra.pdf normal.pdf uniform_edit_cost.pdf
