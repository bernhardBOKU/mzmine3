/*
 * Copyright (c) 2004-2024 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.ionidnetworking;


import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.ionidnetworking.IonNetworkLibrary.CheckMode;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.refinement.IonNetworkRefinementParameters;
import io.github.mzmine.parameters.Parameter;
import io.github.mzmine.parameters.impl.IonMobilitySupport;
import io.github.mzmine.parameters.impl.SimpleParameterSet;
import io.github.mzmine.parameters.parametertypes.ComboParameter;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.ionidentity.IonLibraryParameterSet;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsParameter;
import io.github.mzmine.parameters.parametertypes.submodules.OptionalModuleParameter;
import io.github.mzmine.parameters.parametertypes.submodules.SubModuleParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.ToleranceType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class IonNetworkingParameters extends SimpleParameterSet {

  // different depth of settings
  public enum Setup {
    FULL, SUB, SIMPLE
  }

  // NOT INCLUDED in sub
  // General parameters
  public static final FeatureListsParameter PEAK_LISTS = new FeatureListsParameter();

  // INCLUDED in sub
  // MZ-tolerance: deisotoping, adducts
  public static final MZToleranceParameter MZ_TOLERANCE = new MZToleranceParameter(
      ToleranceType.INTRA_SAMPLE);

  public static final ComboParameter<CheckMode> CHECK_MODE = new ComboParameter<CheckMode>("Check",
      "The modes to check for adduct identities. Average compares only the average m/z values (without min. height).\n "
      + "ALL features and SINGLE feature compares the m/z values of features with height>minHeight in raw data files",
      CheckMode.values(), CheckMode.ONE_FEATURE);

  public static final DoubleParameter MIN_HEIGHT = new DoubleParameter("Min height",
      "Minimum height of feature shape (not used for average mode)",
      MZmineCore.getConfiguration().getIntensityFormat(), 0d);


  // adduct finder parameter - taken from the adduct finder
  // search for adducts? Bonus for correlation?
  public static final SubModuleParameter<IonLibraryParameterSet> LIBRARY = new SubModuleParameter<>(
      "Ion identity library", "Adducts, in-source fragments and multimers",
      new IonLibraryParameterSet());

  // MS MS
  // check for truth MS/MS
  // public static final OptionalModuleParameter<IonNetworkMSMSCheckParameters> MSMS_CHECK =
  // new OptionalModuleParameter<IonNetworkMSMSCheckParameters>("Check MS/MS",
  // "Check MS/MS for truth of multimers", new IonNetworkMSMSCheckParameters(true));

  public static final OptionalModuleParameter<IonNetworkRefinementParameters> ANNOTATION_REFINEMENTS = new OptionalModuleParameter<IonNetworkRefinementParameters>(
      "Annotation refinement", "", new IonNetworkRefinementParameters(true), true);

  // setup
  private final Setup setup;

  // Constructor
  public IonNetworkingParameters() {
    this(Setup.FULL);
  }

  public IonNetworkingParameters(Setup setup) {
    super(createParam(setup));
    this.setup = setup;
  }

  private static Parameter[] createParam(Setup setup) {
    switch (setup) {
      case FULL:
        return new Parameter[]{PEAK_LISTS, MZ_TOLERANCE, CHECK_MODE, MIN_HEIGHT, LIBRARY,
            ANNOTATION_REFINEMENTS};
      case SUB:
        return new Parameter[]{MZ_TOLERANCE, CHECK_MODE, LIBRARY, ANNOTATION_REFINEMENTS};
      case SIMPLE:
        return new Parameter[]{CHECK_MODE, LIBRARY};
    }
    return new Parameter[0];
  }

  /**
   * Create full set of parameters
   *
   * @param param
   * @return
   */
  public static IonNetworkingParameters createFullParamSet(IonNetworkingParameters param,
      double minHeight) {
    return createFullParamSet(param, null, minHeight);
  }

  /**
   * Create full set of parameters
   */
  public static IonNetworkingParameters createFullParamSet(IonNetworkingParameters param,
      MZTolerance mzTol, double minHeight) {
    IonNetworkingParameters full = new IonNetworkingParameters();
    for (Parameter p : param.getParameters()) {
      full.getParameter(p).setValue(p.getValue());
    }
    if (mzTol != null) {
      full.getParameter(IonNetworkingParameters.MZ_TOLERANCE).setValue(mzTol);
    }

    full.getParameter(IonNetworkingParameters.MIN_HEIGHT).setValue(minHeight);
    return full;
  }

  /**
   * The setup mode
   *
   * @return
   */
  public Setup getSetup() {
    return setup;
  }

  @Override
  public @NotNull IonMobilitySupport getIonMobilitySupport() {
    return IonMobilitySupport.SUPPORTED;
  }

  @Override
  public Map<String, Parameter<?>> getNameParameterMap() {
    // parameters were renamed but stayed the same type
    var nameParameterMap = super.getNameParameterMap();
    // we use the same parameters here so no need to increment the version. Loading will work fine
    nameParameterMap.put("m/z tolerance", getParameter(MZ_TOLERANCE));
    return nameParameterMap;
  }
}
