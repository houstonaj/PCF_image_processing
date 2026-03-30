# QuPath groovy Scripts and cell type distance calculations

Custom scripts for spatial proteomics image processing in QuPath and R

## Scripts
cell_detection.groovy: Uses a trained nuclei segmentation model to detect all DAPI-positive nuclei with approximated cell boundaries from an input PhenoCycler Fusion qptiff image. 

cell_type_classification.groovy: Established single-classifier thresholds for cell type assignments based on marker positivity for a given input region. 

PCF_cell_type_analysis.Rmd: All associated packages and scripts to use the assigned cell types and coordinates to perform distance calculations in R. 

## Usage
1. Open QuPath and load in QPTIFF images.
2. Go to Automate → Show Script Editor
3. Load and run the scripts cell_detection then cell_type_classification
4. Save output of cell type assignments from each image with associated pixel measurements.  
5. Open Rv4.4.3, install any necessary packages, load in the ouput measurement tables.
6. Run the PCF_cell_type_analysis script for distance calculations. 

## Requirements
QuPath v0.6+ and R v4.4.3
