import { FrameViewer } from './frameViewer';

const canvas = document.getElementById('viewer') as HTMLCanvasElement;
const viewer = new FrameViewer(canvas);
viewer.start();
