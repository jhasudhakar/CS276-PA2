pdf: fig report.tex
	pdflatex report.tex
	pdflatex report.tex && open report.pdf
fig: extra.py normal.py uniform_edit_cost.py
	python extra.py
	python normal.py
	python uniform_edit_cost.py
clean:
	rm report.aux report.log report.pdf extra.pdf normal.pdf uniform_edit_cost.pdf
