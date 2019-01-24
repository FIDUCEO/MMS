#!/bin/bash

# this is the DOI for the MW FCDR, please modify to use your own!!!
DOI='10.5285/8e9f44965434f3b861eba77688701ef'
AUTH='Centre for Environmental Data Analysis (CEDA)'

path=$1

FILES=$path/*.nc

for f in $FILES
do
        ncatted -a id,global,m,c,"${DOI}" -a naming_authority,global,m,c,"${AUTH}" -h $f
        echo $f
done
