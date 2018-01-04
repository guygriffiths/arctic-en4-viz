import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.dataset.cdm.En3DatasetFactory;
import uk.ac.rdg.resc.edal.graphics.style.ColouredGlyphLayer;
import uk.ac.rdg.resc.edal.graphics.style.MapImage;
import uk.ac.rdg.resc.edal.graphics.style.ScaleRange;
import uk.ac.rdg.resc.edal.graphics.style.SegmentColourScheme;
import uk.ac.rdg.resc.edal.graphics.style.util.SimpleFeatureCatalogue;
import uk.ac.rdg.resc.edal.grid.RegularGridImpl;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;

/*******************************************************************************
 * Copyright (c) 2016 The University of Reading All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * University of Reading, nor the names of the authors or contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

public class RenderEN4 {

    public static void main(String[] args) throws Exception {
        String outputPath = "/home/guy/arctic-out/";

        int size = 810;
        RegularGridImpl npGrid = new RegularGridImpl(-2500000, -2500000, 6500000, 6500000,
                GISUtils.getCrs("EPSG:5041"), size, size);

        /*
         * Read the background blue marble image
         */
        BufferedImage npBackground = ImageIO.read(RenderEN4.class.getResource("/5041.png"));

        /*
         * Create a new composite image with an SST layer and an ice layer
         */
        MapImage compositeImage = new MapImage();
        compositeImage.getLayers().add(
                new ColouredGlyphLayer("POTM_CORRECTED", "dot", new SegmentColourScheme(
                        new ScaleRange(0f, 1.0f, false), Color.white, Color.white, new Color(0,
                                true), "#ffffff,#ffffff", 2)));
        En3DatasetFactory datasetFactory = new En3DatasetFactory();

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(npBackground, 0, 0, size, size, 0, 0, npBackground.getWidth(),
                npBackground.getHeight(), null);
        for (int startYear = 1960; startYear <= 2010; startYear += 5) {
//            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D g = image.createGraphics();
//            g.drawImage(npBackground, 0, 0, size, size, 0, 0, npBackground.getWidth(),
//                    npBackground.getHeight(), null);

            String regexp = "/home/guy/Data/EN4/EN.4.1.1.f.profiles.g10.";
            if (startYear % 10 == 0) {
                regexp += ((int) (startYear / 10)) + "[01234]*.nc";
            } else {
                regexp += ((int) (startYear / 10)) + "[56789]*.nc";
            }
            System.out.println(regexp);
            Dataset en4 = datasetFactory.createDataset("en4" + startYear, regexp);
            SimpleFeatureCatalogue<Dataset> catalogue = new SimpleFeatureCatalogue<>(en4, false);
            for (int year = startYear; year < startYear + 5; year++) {
                PlottingDomainParams npParams = new PlottingDomainParams(npGrid, null,
                        Extents.newExtent(new DateTime(year, 1, 1, 0, 0), new DateTime(year, 12,
                                31, 0, 0)), null, null, null);
                BufferedImage npSstImage = compositeImage.drawImage(npParams, catalogue);
                g.drawImage(npSstImage, 0, 0, size, size, null);
            }

            /*
             * Write the image to disk
             */
//            ImageIO.write(image, "png", new File(outputPath + "/arctic_" + startYear + "-"
//                    + (startYear + 5) + ".png"));
        }
        ImageIO.write(image, "png", new File(outputPath + "/arctic_total.png"));
    }
}
