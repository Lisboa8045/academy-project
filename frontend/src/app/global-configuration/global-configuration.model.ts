import {ConfigType} from './config-type.enum';

export interface GlobalConfiguration {
  configName: string;
  configKey: string;
  configValue: string;
  configType: ConfigType;
}
