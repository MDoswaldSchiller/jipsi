/**
 * Copyright (C) 2003 <a href="http://www.lohndirekt.de/">lohndirekt.de</a>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package jipsi.de.lohndirekt.print.attribute;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.print.attribute.Attribute;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Compression;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.print.attribute.standard.DateTimeAtCreation;
import javax.print.attribute.standard.DateTimeAtProcessing;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.Finishings;
import javax.print.attribute.standard.JobImpressions;
import javax.print.attribute.standard.JobImpressionsCompleted;
import javax.print.attribute.standard.JobImpressionsSupported;
import javax.print.attribute.standard.JobKOctets;
import javax.print.attribute.standard.JobKOctetsProcessed;
import javax.print.attribute.standard.JobKOctetsSupported;
import javax.print.attribute.standard.JobMediaSheets;
import javax.print.attribute.standard.JobMediaSheetsCompleted;
import javax.print.attribute.standard.JobMediaSheetsSupported;
import javax.print.attribute.standard.JobMessageFromOperator;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobOriginatingUserName;
import javax.print.attribute.standard.JobPriority;
import javax.print.attribute.standard.JobPrioritySupported;
import javax.print.attribute.standard.JobSheets;
import javax.print.attribute.standard.JobState;
import javax.print.attribute.standard.JobStateReason;
import javax.print.attribute.standard.JobStateReasons;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MultipleDocumentHandling;
import javax.print.attribute.standard.NumberOfDocuments;
import javax.print.attribute.standard.NumberOfInterveningJobs;
import javax.print.attribute.standard.NumberUp;
import javax.print.attribute.standard.NumberUpSupported;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.OutputDeviceAssigned;
import javax.print.attribute.standard.PDLOverrideSupported;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PagesPerMinute;
import javax.print.attribute.standard.PagesPerMinuteColor;
import javax.print.attribute.standard.PresentationDirection;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterInfo;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterLocation;
import javax.print.attribute.standard.PrinterMakeAndModel;
import javax.print.attribute.standard.PrinterMessageFromOperator;
import javax.print.attribute.standard.PrinterMoreInfo;
import javax.print.attribute.standard.PrinterMoreInfoManufacturer;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.PrinterState;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import javax.print.attribute.standard.PrinterURI;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Severity;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import jipsi.de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import jipsi.de.lohndirekt.print.attribute.cups.DeviceClass;
import jipsi.de.lohndirekt.print.attribute.cups.DeviceUri;
import jipsi.de.lohndirekt.print.attribute.cups.JobKLimit;
import jipsi.de.lohndirekt.print.attribute.cups.JobPageLimit;
import jipsi.de.lohndirekt.print.attribute.cups.JobQuotaPeriod;
import jipsi.de.lohndirekt.print.attribute.cups.MemberNames;
import jipsi.de.lohndirekt.print.attribute.cups.MemberUris;
import jipsi.de.lohndirekt.print.attribute.cups.PrinterType;
import jipsi.de.lohndirekt.print.attribute.ipp.Charset;
import jipsi.de.lohndirekt.print.attribute.ipp.DetailedStatusMessage;
import jipsi.de.lohndirekt.print.attribute.ipp.DocumentFormat;
import jipsi.de.lohndirekt.print.attribute.ipp.NaturalLanguage;
import jipsi.de.lohndirekt.print.attribute.ipp.StatusMessage;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobId;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobMoreInfo;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobOriginatingHostName;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobPrinterUpTime;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobPrinterUri;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobUri;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.TimeAtCompleted;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.TimeAtCreation;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.TimeAtProcessing;
import jipsi.de.lohndirekt.print.attribute.ipp.jobtempl.LdJobHoldUntil;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.MultipleOperationTimeout;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.NaturalLanguageConfigured;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.PrinterCurrentTime;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.PrinterDriverInstaller;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.PrinterStateMessage;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.PrinterUpTime;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.CharsetConfigured;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.CopiesDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.DocumentFormatDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.FinishingsDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.JobHoldUntilDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.JobPriorityDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.JobSheetsDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.MediaDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.MultipleDocumentHandlingDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.NumberUpDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.OrientationRequestedDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults.SidesDefault;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.CharsetSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.CompressionSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.DocumentFormatSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.FinishingsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.GeneratedNaturalLanguageSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.IppVersionsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.JobHoldUntilSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.JobSheetsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.MediaSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.MultipleDocumentHandlingSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.MultipleDocumentJobsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OrientationRequestedSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OutputBinSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.PageRangesSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.PrinterUriSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.SidesSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.UriAuthenticationSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.UriSecuritySupported;
import jipsi.de.lohndirekt.print.attribute.undocumented.PrinterStateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch
 *
 */
public enum IppAttributeName
{
  /*
	 * Attributes defined in javax.print.attribute.standard
   */
  COLOR_SUPPORTED(ColorSupported.SUPPORTED),
  COMPRESSION(Compression.NONE),
  COPIES(new Copies(1)),
  COPIES_SUPPORTED(new CopiesSupported(1)),
  DATE_TIME_AT_COMPLETION(new DateTimeAtCompleted(new Date())),
  DATE_TIME_AT_CREATION(new DateTimeAtCreation(new Date())),
  DATE_TIME_AT_PROCESSING(new DateTimeAtProcessing(new Date())),
  DOCUMENT_NAME(new DocumentName("", Locale.CANADA)),
  FIDELITY(Fidelity.FIDELITY_TRUE),
  FINISHINGS(Finishings.BIND),
  JOB_IMPRESSIONS(new JobImpressions(1)),
  JOB_IMPRESSIONS_COMPLETED(new JobImpressionsCompleted(1)),
  JOB_IMPRESSIONS_SUPPORTED(new JobImpressionsSupported(1, 1)),
  JOB_K_OCTETS(new JobKOctets(1)),
  JOB_K_OCTETS_PROCESSED(new JobKOctetsProcessed(1)),
  JOB_K_OCTETS_SUPPORTED(new JobKOctetsSupported(1, 1)),
  JOB_MEDIA_SHEETS(new JobMediaSheets(1)),
  JOB_MEDIA_SHEETS_COMPLETED(new JobMediaSheetsCompleted(1)),
  JOB_MEDIA_SHEETS_SUPPORTED(new JobMediaSheetsSupported(1, 1)),
  JOB_MESSAGE_FROM_OPERATOR(new JobMessageFromOperator("", Locale.CANADA)),
  JOB_NAME(new JobName("", Locale.CANADA)),
  JOB_ORIGINATING_USER_NAME(new JobOriginatingUserName("", Locale.CANADA)),
  JOB_PRIORIY(new JobPriority(1)),
  JOB_PRIORIY_SUPPORTED(new JobPrioritySupported(1)),
  JOB_SHEETS(JobSheets.NONE),
  JOB_STATE(JobState.ABORTED),
  JOB_STATE_REASON(JobStateReason.ABORTED_BY_SYSTEM),
  JOB_STATE_REASONS(new JobStateReasons()),
  
  //Could be MediaName,MediaSizeName or MediaTray
  MEDIA(MediaSizeName.A),
  MULTIPLE_DOCUMENT_HANDLING(MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES),
  NUMBER_OF_DOCUMENTS(new NumberOfDocuments(1)),
  NUMBER_OF_INTERVENING_JOBS(new NumberOfInterveningJobs(1)),
  NUMBER_UP(new NumberUp(1)),
  NUMBER_UP_SUPPORTED(new NumberUpSupported(1)),
  ORIENTATION_REQUESTED(OrientationRequested.LANDSCAPE),
  OUTPUT_DEVICE_ASSIGNED(new OutputDeviceAssigned("", Locale.CANADA)),
  PAGE_RANGES(new PageRanges(1)),
  PAGES_PER_MINUTE(new PagesPerMinute(1)),
  PAGES_PER_MINUTE_COLOR(new PagesPerMinuteColor(1)),
  PDL_OVERRIDE_SUPPORTED(PDLOverrideSupported.ATTEMPTED),
  PRESENTATION_DIRECTION(PresentationDirection.TOBOTTOM_TOLEFT),
  PRINTER_INFO(new PrinterInfo("", Locale.CANADA)),
  PRINTER_IS_ACCEPTING_JOBS(PrinterIsAcceptingJobs.ACCEPTING_JOBS),
  PRINTER_LOCATION(new PrinterLocation("", Locale.CANADA)),
  PRINTER_MAKE_AND_MODEL(new PrinterMakeAndModel("", Locale.CANADA)),
  PRINTER_MESSAGE_FROM_OPERATOR(new PrinterMessageFromOperator("", Locale.CANADA)),
  PRINTER_MORE_INFO(new PrinterMoreInfo(IppAttributeName.getURI())),
  PRINTER_MORE_INFO_MANUFACTURER(new PrinterMoreInfoManufacturer(IppAttributeName.getURI())),
  PRINTER_NAME(new PrinterName("", Locale.CANADA)),
  PRINTER_RESOLUTION(new PrinterResolution(1, 1, 1)),
  PRINTER_STATE(PrinterState.IDLE),
  PRINTER_STATE_REASON(PrinterStateReason.CONNECTING_TO_DEVICE),
  PRINTER_STATE_REASONS(new PrinterStateReasons()),
  PRINTER_URI(new PrinterURI(IppAttributeName.getURI())),
  PRINT_QUALITY(PrintQuality.DRAFT),
  QUEUED_JOB_COUNT(new QueuedJobCount(1)),
  REFERENCE_URI_SCHEMES_SUPPORTED(ReferenceUriSchemesSupported.FILE),
  REQUESTING_USER_NAME(new RequestingUserName("", Locale.CANADA)),
  REQUESTING_USER_PASSWD(new RequestingUserPassword("", Locale.CANADA)),
  SEVERITY(Severity.ERROR),
  SHEET_COLLATE(SheetCollate.COLLATED),
  SIDES(Sides.DUPLEX),

  /*
	 * IPP standard attributes defined in de.lohndirekt.attribute.ipp
   */
  CHARSET(new Charset("x", Locale.getDefault())),
  CHARSET_CONFIGURED(new CharsetConfigured("x", Locale.getDefault())),
  CHARSET_SUPORTED(new CharsetSupported("x", Locale.getDefault())),
  COMPRESSION_SUPORTED(new CompressionSupported("x", Locale.getDefault())),
  COPIES_DEFAULT(new CopiesDefault(1)),
  DETAILED_STATUS_MESSAGE(new DetailedStatusMessage("x", Locale.getDefault())),
  DOCUMENT_FORMAT(new DocumentFormat("x", Locale.getDefault())),
  DOCUMENT_FORMAT_SUPORTED(new DocumentFormatSupported("x", Locale.getDefault())),
  DOCUMENT_FORMAT_DEFAULT(new DocumentFormatDefault("x", Locale.getDefault())),
  FINISHINGS_DEFAULT(new FinishingsDefault(1)),
  FINISHINGS_SUPPORTED(new FinishingsSupported(1)),
  IPP_VERSIONS_SUPPORTED(new IppVersionsSupported("x", Locale.getDefault())),
  JOB_HOLD_UNTIL(new LdJobHoldUntil("x", Locale.getDefault())),
  JOB_HOLD_UNTIL_DEFAULT(new JobHoldUntilDefault("x", Locale.getDefault())),
  JOB_HOLD_UNTIL_SUPPORTED(new JobHoldUntilSupported("x", Locale.getDefault())),
  JOB_ID(new JobId(1)),
  JOB_MORE_INFO(new JobMoreInfo(IppAttributeName.getURI())),
  JOB_ORIGINATING_HOST_NAME(new JobOriginatingHostName("x", Locale.getDefault())),
  JOB_PRINTER_UP_TIME(new JobPrinterUpTime(1)),
  JOB_PRINTER_URI(new JobPrinterUri(getURI())),
  JOB_PRIORITY_DEFAULT(new JobPriorityDefault(1)),
  JOB_SHEETS_DEFAULT(new JobSheetsDefault("x", Locale.getDefault())),
  JOB_SHEETS_SUPORTED(new JobSheetsSupported("x", Locale.getDefault())),
  JOB_URI(new JobUri(IppAttributeName.getURI())),
  GENERATED_NATURAL_LANGUAGE_SUPPORTED(new GeneratedNaturalLanguageSupported("x", Locale.getDefault())),
  MEDIA_DEFAULT(new MediaDefault("x", Locale.getDefault())),
  MEDIA_SUPPORTED(new MediaSupported("x", Locale.getDefault())),
  MULTIPLE_DOCUMENT_HANDLING_DEFAULT(new MultipleDocumentHandlingDefault("x", Locale.getDefault())),
  MULTIPLE_DOCUMENT_HANDLING_SUPPORTED(new MultipleDocumentHandlingSupported("x", Locale.getDefault())),
  MULTIPLE_DOCUMENT_JOBS_SUPPORTED(new MultipleDocumentJobsSupported(1)),
  MULTIPLE_OPERATION_TIMEOUT(new MultipleOperationTimeout(1)),
  NATURAL_LANGUAGE(new NaturalLanguage("x", Locale.getDefault())),
  NATURAL_LANGUAGE_CONFIGURED(new NaturalLanguageConfigured("x", Locale.getDefault())),
  NUMBER_UP_DEFAULT(new NumberUpDefault(1)),
  OPERATIONS_SUPPORTED(new OperationsSupported(1)),
  ORIENTATION_REQUESTED_DEFAULT(new OrientationRequestedDefault(1)),
  ORIENTATION_REQUESTED_SUPPORTED(new OrientationRequestedSupported(1)),
  PAGE_RANGES_SUPPORTED(new PageRangesSupported(1)),
  PRINTER_CURRENT_TIME(new PrinterCurrentTime(new Date())),
  PRINTER_DRIVER_INSTALLER(new PrinterDriverInstaller(IppAttributeName.getURI())),
  PRINTER_STATE_MESSAGE(new PrinterStateMessage("x", Locale.getDefault())),
  PRINTER_TYPE(new PrinterType(1)),
  PRINTER_UP_TIME(new PrinterUpTime(1)),
  PRINTER_URI_SUPPORTED(new PrinterUriSupported(IppAttributeName.getURI())),
  SIDES_DEFAULT(new SidesDefault("x", Locale.getDefault())),
  SIDES_SUPPORTED(new SidesSupported("x", Locale.getDefault())),
  STATUS_MESSAGE(new StatusMessage("x", Locale.getDefault())),
  TIME_AT_COMPLETED(new TimeAtCompleted(1)),
  TIME_AT_CREATION(new TimeAtCreation(1)),
  TIME_AT_PROCESSING(new TimeAtProcessing(1)),
  URI_AUTHENTICATION_SUPPORTED(new UriAuthenticationSupported("x", Locale.getDefault())),
  URI_SECURITY_SUPPORTED(new UriSecuritySupported("x", Locale.getDefault())),

  /*
	 * CUPS IPP extension attributes defined in de.lohndirekt.attribute.cups
   */
  DEVICE_CLASS(new DeviceClass("x", Locale.getDefault())),
  DEVICE_URI(new DeviceUri(IppAttributeName.getURI())),
  Job_K_LIMIT(new JobKLimit(1)),
  JOB_PAGE_LIMIT(new JobPageLimit(1)),
  JOB_QUOTA_PERIOD(new JobQuotaPeriod(1)),
  MEMBER_NAMES(new MemberNames("x", Locale.getDefault())),
  MEMBER_URIS(new MemberUris(IppAttributeName.getURI())),
  PRINTER_STATE_TIME(new PrinterStateTime(1)),

  /*
	 * undocumented IPP attributes used by CUPS
   */
  OUTPUT_BIN_SUPPORTED(new OutputBinSupported("x", Locale.getDefault()));

  private static final Logger LOG = LoggerFactory.getLogger(IppAttributeName.class);
  private static final Map<String,IppAttributeName> CACHE;
  static {
    Map<String,IppAttributeName> cache = new HashMap<>();
    for (IppAttributeName entry : values()) {
      cache.put(entry.getName(), entry);
    }
    CACHE = Map.copyOf(cache);
  }

	/**
		 *
		 */
	private static URI getURI()
  {
    try {
      return new URI("http://www.lohndirekt.de");
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

  }

  public static IppAttributeName get(String attributeName)
  {
    return CACHE.get(attributeName);
  }

  // End of static part

  private final String methodName;
  private final Class attributeClass;
  private final Class category;

  private IppAttributeName(Attribute attribute)
  {
    this.methodName = attribute.getName();
    this.attributeClass = attribute.getClass();
    this.category = attribute.getCategory();
  }

  /**
   * @return
   */
  public Class getAttributeClass()
  {
    return attributeClass;
  }

  public String getName()
  {
    return this.methodName;
  }

  public Class getCategory()
  {
    return this.category;
  }
}
