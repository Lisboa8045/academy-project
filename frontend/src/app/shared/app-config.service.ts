import {Injectable} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  readonly imageUploadConfig = {
    maxSizeMB: 10,
    maxWidth: 3000,
    maxHeight: 3000
  };

  readonly messages = {
    fileTooLarge: (max: number) => `File is too large. Max allowed size is ${max}MB.`,
    imageTooBig: (w: number, h: number) => `Image too large. Max dimensions are ${w}x${h}px.`
  };
}
