// Copyright 2022 KURZ Digital Solutions GmbH
//
// SPDX-License-Identifier: Apache-2.0

#import "ZXIDecodeHints.h"
#import "DecodeHints.h"

@interface ZXIDecodeHints()
@property(nonatomic) ZXing::DecodeHints zxingHints;
@end

@implementation ZXIDecodeHints

-(instancetype)init {
    self = [super init];
    self.zxingHints = ZXing::DecodeHints();
    return self;
}

- (instancetype)initWithTryHarder:(BOOL)tryHarder
                        tryRotate:(BOOL)tryRotate
                     tryDownscale:(BOOL)tryDownscale
               maxNumberOfSymbols:(NSInteger)maxNumberOfSymbols
                        tryInvert:(BOOL)tryInvert
            tryCode39ExtendedMode:(BOOL)tryCode39ExtendedMode
           validateCode39CheckSum:(BOOL)validateCode39CheckSum
              validateITFCheckSum:(BOOL)validateITFCheckSum
                  downscaleFactor:(uint8_t)downscaleFactor
               downscaleThreshold:(uint16_t)downscaleThreshold
                          formats:(NSArray<NSNumber*>*)formats {
    self = [super init];
    self.zxingHints = ZXing::DecodeHints();
    self.tryHarder = tryHarder;
    self.tryRotate = tryRotate;
    self.tryDownscale = tryDownscale;
    self.tryInvert = tryInvert;
    self.maxNumberOfSymbols = maxNumberOfSymbols;
    self.tryCode39ExtendedMode = tryCode39ExtendedMode;
    self.validateCode39CheckSum = validateCode39CheckSum;
    self.validateITFCheckSum = validateITFCheckSum;
    self.downscaleFactor = downscaleFactor;
    self.downscaleThreshold = downscaleThreshold;
    self.formats = formats;
    return self;
}

-(void)setTryHarder:(BOOL)tryHarder {
    self.zxingHints.setTryHarder(tryHarder);
}

-(void)setTryRotate:(BOOL)tryRotate {
    self.zxingHints.setTryRotate(tryRotate);
}

-(void)setTryDownscale:(BOOL)tryDownscale {
    self.zxingHints.setTryDownscale(tryDownscale);
}

-(void)setTryInvert:(BOOL)tryInvert {
    self.zxingHints.setTryInvert(tryInvert);
}

-(void)setMaxNumberOfSymbols:(NSInteger)maxNumberOfSymbols {
    self.zxingHints.setMaxNumberOfSymbols(maxNumberOfSymbols);
}

-(void)setTryCode39ExtendedMode:(BOOL)tryCode39ExtendedMode {
    self.zxingHints.setTryCode39ExtendedMode(tryCode39ExtendedMode);
}

-(void)setValidateCode39CheckSum:(BOOL)validateCode39CheckSum {
    self.zxingHints.setValidateCode39CheckSum(validateCode39CheckSum);
}

-(void)setValidateITFCheckSum:(BOOL)validateITFCheckSum {
    self.zxingHints.setValidateITFCheckSum(validateITFCheckSum);
}

-(void)setDownscaleFactor:(uint8_t)downscaleFactor {
    self.zxingHints.setDownscaleFactor(downscaleFactor);
}

-(void)setDownscaleThreshold:(uint16_t)downscaleThreshold {
    self.zxingHints.setDownscaleThreshold(downscaleThreshold);
}

-(BOOL)tryHarder {
    return self.zxingHints.tryHarder();
}

-(BOOL)tryRotate {
    return self.zxingHints.tryRotate();
}

-(BOOL)tryDownscale {
    return self.zxingHints.tryDownscale();
}

-(BOOL)tryInvert {
    return self.zxingHints.tryInvert();
}

- (NSInteger)maxNumberOfSymbols {
    return self.zxingHints.maxNumberOfSymbols();
}

-(BOOL)tryCode39ExtendedMode {
    return self.zxingHints.tryCode39ExtendedMode();
}

-(BOOL)validateCode39CheckSum {
    return self.zxingHints.validateCode39CheckSum();
}

-(BOOL)validateITFCheckSum {
    return self.zxingHints.validateITFCheckSum();
}

-(uint8_t)downscaleFactor {
    return self.zxingHints.downscaleFactor();
}

-(uint16_t)downscaleThreshold {
    return self.zxingHints.downscaleThreshold();
}

@end
